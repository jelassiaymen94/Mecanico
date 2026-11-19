package com.mecanico.marketplace.service;

import com.mecanico.marketplace.dto.ListingRequest;
import com.mecanico.marketplace.dto.ListingResponse;
import com.mecanico.marketplace.entity.ItemCondition;
import com.mecanico.marketplace.entity.Listing;
import com.mecanico.marketplace.entity.ListingCategory;
import com.mecanico.marketplace.entity.ListingStatus;
import com.mecanico.marketplace.exception.ResourceNotFoundException;
import com.mecanico.marketplace.repository.ListingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListingServiceTest {

    @Mock
    private ListingRepository listingRepository;

    @InjectMocks
    private ListingService listingService;

    @Test
    void createShouldForcePendingVerificationStatus() {
        ListingRequest request = new ListingRequest(
                "seller-1",
                "Engine",
                "Used engine in good condition",
                ListingCategory.ENGINE,
                "Renault",
                "Clio 3",
                ItemCondition.USED,
                new BigDecimal("1200.00"),
                2,
                "Tunis"
        );
        when(listingRepository.save(any(Listing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ListingResponse response = listingService.create(request);

        assertThat(response.status()).isEqualTo(ListingStatus.PENDING_VERIFICATION);
        assertThat(response.sellerId()).isEqualTo("seller-1");
        assertThat(response.title()).isEqualTo("Engine");
        assertThat(response.price()).isEqualByComparingTo("1200.00");
    }

    @Test
    void getShouldThrowWhenListingDoesNotExist() {
        when(listingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> listingService.get(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateShouldKeepExistingStatus() {
        Listing listing = new Listing(
                "seller-1",
                "Engine",
                "Old description",
                ListingCategory.ENGINE,
                "Renault",
                "Clio 3",
                ItemCondition.USED,
                new BigDecimal("1200.00"),
                2,
                "Tunis"
        );
        listing.setStatus(ListingStatus.VERIFIED);
        when(listingRepository.findById(1L)).thenReturn(Optional.of(listing));
        when(listingRepository.save(listing)).thenReturn(listing);

        ListingRequest request = new ListingRequest(
                "seller-1",
                "Updated engine",
                "Updated description",
                ListingCategory.ENGINE,
                "Renault",
                "Clio 3",
                ItemCondition.USED,
                new BigDecimal("1100.00"),
                3,
                "Sfax"
        );

        ListingResponse response = listingService.update(1L, request);

        assertThat(response.status()).isEqualTo(ListingStatus.VERIFIED);
        assertThat(response.title()).isEqualTo("Updated engine");
        assertThat(response.quantity()).isEqualTo(3);
        assertThat(response.city()).isEqualTo("Sfax");
    }
}
