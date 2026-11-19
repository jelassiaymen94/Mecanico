package com.mecanico.marketplace;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MarketplaceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listingAndOrderCrudFlow() throws Exception {
        String listingJson = """
                {
                  "sellerId": "seller-1",
                  "title": "Engine",
                  "description": "Used engine in good condition",
                  "category": "ENGINE",
                  "brand": "Renault",
                  "compatibleModels": "Clio 3",
                  "condition": "USED",
                  "price": 1200.00,
                  "quantity": 2,
                  "city": "Tunis"
                }
                """;

        MvcResult listingResult = mockMvc.perform(post("/api/v1/listings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(listingJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_VERIFICATION"))
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        long listingId = readId(listingResult);

        mockMvc.perform(get("/api/v1/listings/{id}", listingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Engine"))
                .andExpect(jsonPath("$.status").value("PENDING_VERIFICATION"));

        mockMvc.perform(get("/api/v1/listings")
                        .param("city", "Tunis")
                        .param("category", "ENGINE")
                        .param("q", "engine")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].sellerId").value("seller-1"))
                .andExpect(jsonPath("$.totalElements").value(1));

        String updatedListingJson = """
                {
                  "sellerId": "seller-1",
                  "title": "Updated engine",
                  "description": "Updated description",
                  "category": "ENGINE",
                  "brand": "Renault",
                  "compatibleModels": "Clio 3",
                  "condition": "USED",
                  "price": 1200.00,
                  "quantity": 2,
                  "city": "Tunis"
                }
                """;

        mockMvc.perform(put("/api/v1/listings/{id}", listingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedListingJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated engine"))
                .andExpect(jsonPath("$.status").value("PENDING_VERIFICATION"));

        String orderJson = """
                {
                  "listingId": %d,
                  "buyerId": "buyer-1",
                  "buyerType": "INDIVIDUAL",
                  "quantity": 2
                }
                """.formatted(listingId);

        MvcResult orderResult = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalPrice").value(2400.0))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        long orderId = readId(orderResult);

        mockMvc.perform(get("/api/v1/orders").param("buyerId", "buyer-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].buyerId").value("buyer-1"));

        mockMvc.perform(get("/api/v1/orders")
                        .param("buyerId", "buyer-1")
                        .param("listingId", String.valueOf(listingId))
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].listingId").value(listingId));

        String updatedOrderJson = """
                {
                  "buyerType": "PROFESSIONAL",
                  "quantity": 1,
                  "status": "CONFIRMED"
                }
                """;

        mockMvc.perform(put("/api/v1/orders/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedOrderJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(1))
                .andExpect(jsonPath("$.totalPrice").value(1200.0))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.buyerType").value("PROFESSIONAL"));

        mockMvc.perform(delete("/api/v1/orders/{id}", orderId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/orders/{id}", orderId))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidListingShouldReturnValidationErrors() throws Exception {
        String invalidListingJson = """
                {
                  "sellerId": "",
                  "title": "",
                  "description": "",
                  "category": null,
                  "condition": null,
                  "price": -10.00,
                  "quantity": -1,
                  "city": ""
                }
                """;

        mockMvc.perform(post("/api/v1/listings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidListingJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.sellerId").exists())
                .andExpect(jsonPath("$.fieldErrors.title").exists())
                .andExpect(jsonPath("$.fieldErrors.price").exists());
    }

    private long readId(MvcResult result) throws Exception {
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("id").asLong();
    }
}
