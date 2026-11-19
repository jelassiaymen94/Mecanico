package com.mecanico.marketplace.service;

import com.mecanico.marketplace.dto.ListingRequest;
import com.mecanico.marketplace.dto.ListingResponse;
import com.mecanico.marketplace.dto.PageResponse;
import com.mecanico.marketplace.entity.Listing;
import com.mecanico.marketplace.entity.ListingCategory;
import com.mecanico.marketplace.entity.ListingStatus;
import com.mecanico.marketplace.exception.ResourceNotFoundException;
import com.mecanico.marketplace.repository.ListingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Transactional
public class ListingService {

    private final ListingRepository listingRepository;

    public ListingService(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    public ListingResponse create(ListingRequest request) {
        Listing listing = new Listing(
                request.sellerId(),
                request.title(),
                request.description(),
                request.category(),
                request.brand(),
                request.compatibleModels(),
                request.condition(),
                request.price(),
                request.quantity(),
                request.city()
        );
        listing.setStatus(ListingStatus.PENDING_VERIFICATION);
        return ListingResponse.from(listingRepository.save(listing));
    }

    @Transactional(readOnly = true)
    public ListingResponse get(Long id) {
        return ListingResponse.from(findById(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<ListingResponse> list(
            ListingCategory category,
            String city,
            ListingStatus status,
            String sellerId,
            String q,
            Pageable pageable
    ) {
        Specification<Listing> specification = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        if (category != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("category"), category));
        }
        if (city != null && !city.isBlank()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(criteriaBuilder.lower(root.<String>get("city")), city.toLowerCase(Locale.ROOT)));
        }
        if (status != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("status"), status));
        }
        if (sellerId != null && !sellerId.isBlank()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("sellerId"), sellerId));
        }
        if (q != null && !q.isBlank()) {
            String pattern = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.<String>get("title")), pattern),
                            criteriaBuilder.like(criteriaBuilder.lower(root.<String>get("description")), pattern)
                    ));
        }

        Page<Listing> page = listingRepository.findAll(specification, pageable);
        return PageResponse.from(page.map(ListingResponse::from));
    }

    public ListingResponse update(Long id, ListingRequest request) {
        Listing listing = findById(id);
        apply(listing, request);
        return ListingResponse.from(listingRepository.save(listing));
    }

    public void delete(Long id) {
        Listing listing = findById(id);
        listingRepository.delete(listing);
    }

    private Listing findById(Long id) {
        return listingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Listing " + id + " not found"));
    }

    private void apply(Listing listing, ListingRequest request) {
        listing.setSellerId(request.sellerId());
        listing.setTitle(request.title());
        listing.setDescription(request.description());
        listing.setCategory(request.category());
        listing.setBrand(request.brand());
        listing.setCompatibleModels(request.compatibleModels());
        listing.setCondition(request.condition());
        listing.setPrice(request.price());
        listing.setQuantity(request.quantity());
        listing.setCity(request.city());
    }
}
