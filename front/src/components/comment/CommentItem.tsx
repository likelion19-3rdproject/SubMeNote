'use client';

import { useState } from 'react';
import { CommentResponseDto } from '@/src/types/comment';
import { commentApi } from '@/src/api/commentApi';
import { likeApi } from '@/src/api/likeApi';
import Button from '@/src/components/common/Button';
import Textarea from '@/src/components/common/Textarea';

interface CommentItemProps {
  comment: CommentResponseDto;
  postId: number;
  currentUserId: number | null; // 현재 로그인한 사용자 ID
  onDelete: (commentId: number) => void;
  onReport?: (commentId: number) => void; // 신고 핸들러
  onReload: () => void;
  depth?: number; // 댓글 깊이 (들여쓰기용)
}

export default function CommentItem({
  comment,
  postId,
  currentUserId,
  onDelete,
  onReport,
  onReload,
  depth = 0,
}: CommentItemProps) {
  // 각 CommentItem이 자신의 입력창 상태를 독립적으로 관리
  const [isReplying, setIsReplying] = useState(false);
  const [replyContent, setReplyContent] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [localComment, setLocalComment] = useState(comment);
  
  // 본인 댓글인지 확인 (본인 댓글일 때만 삭제 버튼 표시)
  const isMyComment = currentUserId !== null && currentUserId === localComment.userId;

  const handleSubmitReply = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!replyContent.trim()) return;

    try {
      setIsSubmitting(true);
      await commentApi.createComment(postId, { content: replyContent }, comment.id);
      setReplyContent('');
      setIsReplying(false);
      onReload(); // 댓글 목록 새로고침
    } catch (err: any) {
      alert(err.response?.data?.message || '대댓글 작성에 실패했습니다.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleCancelReply = () => {
    setIsReplying(false);
    setReplyContent('');
  };

  const handleToggleLike = async () => {
    try {
      const result = await likeApi.toggleCommentLike(localComment.id);
      // 댓글 상태 업데이트
      setLocalComment({
        ...localComment,
        likeCount: result.likeCount,
        likedByMe: result.liked,
      });
    } catch (err: any) {
      alert(err.response?.data?.message || '좋아요 처리에 실패했습니다.');
    }
  };

  return (
    <div className={`${depth > 0 ? 'ml-4 border-l-2 border-gray-200 pl-4' : ''}`}>
      <div className="border-b border-gray-100 py-6 last:border-b-0">
        <div className="flex justify-between items-start mb-2">
          <div className="flex-1">
            <p className={`font-normal text-gray-500 mb-2 ${depth > 0 ? 'text-sm' : ''}`}>
              {localComment.nickname}
            </p>
            <p className={`text-gray-900 leading-relaxed ${depth > 0 ? 'text-sm' : ''}`}>
              {localComment.content}
            </p>
          </div>
          <div className="flex gap-2 ml-4">
            <Button
              variant="secondary"
              size="sm"
              onClick={() => setIsReplying(!isReplying)}
              disabled={isSubmitting}
            >
              {isReplying ? '취소' : '답글'}
            </Button>
            {onReport && (
              <Button
                variant="secondary"
                size="sm"
                onClick={() => onReport(localComment.id)}
              >
                신고
              </Button>
            )}
            {isMyComment && (
              <Button
                variant="danger"
                size="sm"
                onClick={() => onDelete(localComment.id)}
              >
                삭제
              </Button>
            )}
          </div>
        </div>
        <div className="flex items-center gap-4 mt-3 mb-4">
          <p className={`text-gray-500 ${depth > 0 ? 'text-xs' : 'text-xs'}`}>
            {new Date(localComment.createdAt).toLocaleDateString('ko-KR', {
              year: 'numeric',
              month: 'long',
              day: 'numeric',
            })}
          </p>
          <button
            onClick={handleToggleLike}
            className={`flex items-center gap-1 text-xs ${
              localComment.likedByMe ? 'text-red-500' : 'text-gray-500'
            } hover:text-red-500 transition-colors`}
          >
            <span>{localComment.likedByMe ? '❤️' : '🤍'}</span>
            <span>{localComment.likeCount}</span>
          </button>
        </div>

        {/* 대댓글 작성 폼 */}
        {isReplying && (
          <div className="mt-4 pb-4">
            <form onSubmit={handleSubmitReply}>
              <Textarea
                value={replyContent}
                onChange={(e) => setReplyContent(e.target.value)}
                placeholder={`${localComment.nickname}님에게 답글 달기...`}
                rows={3}
                disabled={isSubmitting}
                className="mb-2 border-gray-200 focus:border-gray-400 rounded-sm"
              />
              <div className="flex gap-2">
                <Button
                  type="submit"
                  disabled={isSubmitting || !replyContent.trim()}
                  size="sm"
                >
                  {isSubmitting ? '작성 중...' : '답글 작성'}
                </Button>
                <Button
                  type="button"
                  variant="secondary"
                  size="sm"
                  onClick={handleCancelReply}
                >
                  취소
                </Button>
              </div>
            </form>
          </div>
        )}

        {/* 자식 댓글 목록 - 재귀적으로 CommentItem 자신을 호출 */}
        {localComment.children && localComment.children.length > 0 && (
          <div className="mt-4 space-y-0">
            {localComment.children.map((child) => (
              <CommentItem
                key={child.id}
                comment={child}
                postId={postId}
                currentUserId={currentUserId}
                onDelete={onDelete}
                onReport={onReport}
                onReload={onReload}
                depth={depth + 1} // 깊이 증가
              />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

