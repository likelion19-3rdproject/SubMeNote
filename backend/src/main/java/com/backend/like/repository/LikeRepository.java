package com.backend.like.repository;

import com.backend.like.entity.Like;
import com.backend.like.entity.LikeTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LikeRepository extends JpaRepository<Like, Long> {

    boolean existsByUser_IdAndTargetTypeAndTargetId(Long userId, LikeTargetType type, Long targetId);

    long countByTargetTypeAndTargetId(LikeTargetType type, Long targetId);

    void deleteByUser_IdAndTargetTypeAndTargetId(Long userId, LikeTargetType type, Long targetId);

    interface TargetCount {
        Long getTargetId();

        Long getCnt();
    }

    @Query("""
            select l.targetId as targetId, count(l.id) as cnt
            from Like l
            where l.targetType = :type and l.targetId in :ids
            group by l.targetId
            """)
    List<TargetCount> countByTargetIds(@Param("type") LikeTargetType type,
                                       @Param("ids") List<Long> ids
    );

    @Query("""
            select l.targetId
            from Like l
            where l.user.id = :userId and l.targetType = :type and l.targetId in :ids
            """)
    List<Long> findLikedTargetIds(@Param("userId") Long userId,
                                  @Param("type") LikeTargetType type,
                                  @Param("ids") List<Long> ids
    );
}