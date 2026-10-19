package com.grovesoft.boctracker.common.attachment;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByOwnerTypeAndOwnerIdOrderByIdAsc(AttachmentOwner type, Long ownerId);

    List<Attachment> findByOwnerTypeAndOwnerIdInOrderByIdAsc(AttachmentOwner type, Collection<Long> ownerIds);

    long countByOwnerTypeAndOwnerId(AttachmentOwner type, Long ownerId);

    boolean existsByOwnerTypeAndOwnerIdAndSha256(AttachmentOwner type, Long ownerId, String sha256);
}
