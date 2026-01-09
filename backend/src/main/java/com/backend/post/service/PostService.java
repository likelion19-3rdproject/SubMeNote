package com.backend.post.service;

public interface PostService {
    //생성
    Long create(PostCreateRequestDto requestDto, Long userId);
    //목록조회
    List<PostResponseDto> getList();
    //단건상세조회
    PostResponseDto findOne(Long id);
    //수정
    void update(Long postId, Long userId, PostUpdateRequestDto requestDto);
    //삭제
    void delete(Long postId, Long userId);
}
