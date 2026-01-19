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
  const [isEditing, setIsEditing] = useState(false);
  const [editContent, setEditContent] = useState(localComment.content);
  
  // 본인 댓글인지 확인 (본인 댓글일 때만 삭제/수정 버튼 표시)
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

  const handleEditComment = () => {
    setEditContent(localComment.content);
    setIsEditing(true);
  };

  const handleCancelEdit = () => {
    setIsEditing(false);
    setEditContent(localComment.content);
  };

  const handleSubmitEdit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editContent.trim()) {
      alert('댓글 내용을 입력해주세요.');
      return;
    }

    try {
      setIsSubmitting(true);
      const updatedComment = await commentApi.updateComment(localComment.id, { content: editContent });
      setLocalComment({
        ...localComment,
        content: updatedComment.content,
      });
      setIsEditing(false);
    } catch (err: any) {
      alert(err.response?.data?.message || '댓글 수정에 실패했습니다.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className={`${depth > 0 ? 'ml-6 mt-4' : ''}`}>
      <div className={`glass p-4 rounded-xl ${depth > 0 ? 'border-l-4 border-purple-300' : ''}`}>
        <div className="flex items-start gap-3">
          <div className={`${depth > 0 ? 'w-8 h-8' : 'w-10 h-10'} rounded-full bg-gradient-to-r from-blue-400 to-purple-400 flex items-center justify-center text-white font-bold flex-shrink-0 ${depth > 0 ? 'text-sm' : ''}`}>
            {localComment.nickname.charAt(0).toUpperCase()}
          </div>
          <div className="flex-1 min-w-0">
            <div className="flex justify-between items-start mb-2">
              <div className="flex-1">
                <p className={`font-bold text-gray-900 mb-1 ${depth > 0 ? 'text-sm' : ''}`}>
                  {localComment.nickname}
                </p>
                {isEditing ? (
                  <form onSubmit={handleSubmitEdit} className="mt-2">
                    <Textarea
                      value={editContent}
                      onChange={(e) => setEditContent(e.target.value)}
                      placeholder="댓글 내용을 입력하세요..."
                      rows={3}
                      disabled={isSubmitting}
                      className="mb-2"
                    />
                    <div className="flex gap-2">
                      <Button
                        type="submit"
                        disabled={isSubmitting || !editContent.trim()}
                        size="sm"
                      >
                        {isSubmitting ? '💾 저장 중...' : '💾 저장'}
                      </Button>
                      <Button
                        type="button"
                        variant="secondary"
                        size="sm"
                        onClick={handleCancelEdit}
                        disabled={isSubmitting}
                      >
                        ❌ 취소
                      </Button>
                    </div>
                  </form>
                ) : (
                  <p className={`text-gray-700 leading-relaxed ${depth > 0 ? 'text-sm' : ''}`}>
                    {localComment.content}
                  </p>
                )}
              </div>
            </div>
            
            <div className="flex items-center gap-4 mt-3">
              <p className="text-xs text-gray-500">
                {new Date(localComment.createdAt).toLocaleDateString('ko-KR', {
                  year: 'numeric',
                  month: 'long',
                  day: 'numeric',
                })}
              </p>
              <button
                onClick={handleToggleLike}
                className={`flex items-center gap-1 text-sm px-2 py-1 rounded-full transition-all ${
                  localComment.likedByMe 
                    ? 'bg-red-100 text-red-600' 
                    : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                }`}
              >
                <span>{localComment.likedByMe ? '❤️' : '🤍'}</span>
                <span className="font-medium">{localComment.likeCount}</span>
              </button>
              
              {!isEditing && (
                <div className="flex gap-1">
                  {depth === 0 && (
                    <button
                      onClick={() => setIsReplying(!isReplying)}
                      disabled={isSubmitting}
                      className="text-xs px-3 py-1 rounded-full bg-gray-100 text-gray-700 hover:bg-gray-200 transition-colors font-medium"
                    >
                      {isReplying ? '❌' : '💬'} {isReplying ? '취소' : '답글'}
                    </button>
                  )}
                  {isMyComment ? (
                    <>
                      <button
                        onClick={handleEditComment}
                        className="text-xs px-3 py-1 rounded-full bg-blue-100 text-blue-700 hover:bg-blue-200 transition-colors font-medium"
                      >
                        ✏️ 수정
                      </button>
                      <button
                        onClick={() => onDelete(localComment.id)}
                        className="text-xs px-3 py-1 rounded-full bg-red-100 text-red-700 hover:bg-red-200 transition-colors font-medium"
                      >
                        🗑️ 삭제
                      </button>
                    </>
                  ) : (
                    onReport && (
                      <button
                        onClick={() => onReport(localComment.id)}
                        className="text-xs px-3 py-1 rounded-full bg-orange-100 text-orange-700 hover:bg-orange-200 transition-colors font-medium"
                      >
                        🚨 신고
                      </button>
                    )
                  )}
                </div>
              )}
            </div>

            {/* 대댓글 작성 폼 */}
            {isReplying && (
              <div className="mt-4 p-4 bg-purple-50 rounded-xl">
                <form onSubmit={handleSubmitReply}>
                  <Textarea
                    value={replyContent}
                    onChange={(e) => setReplyContent(e.target.value)}
                    placeholder={`${localComment.nickname}님에게 답글 달기...`}
                    rows={3}
                    disabled={isSubmitting}
                    className="mb-2"
                  />
                  <div className="flex gap-2">
                    <Button
                      type="submit"
                      disabled={isSubmitting || !replyContent.trim()}
                      size="sm"
                    >
                      {isSubmitting ? '작성 중...' : '✍️ 답글 작성'}
                    </Button>
                    <Button
                      type="button"
                      variant="secondary"
                      size="sm"
                      onClick={handleCancelReply}
                    >
                      ❌ 취소
                    </Button>
                  </div>
                </form>
              </div>
            )}
          </div>
        </div>

        {/* 자식 댓글 목록 - 재귀적으로 CommentItem 자신을 호출 */}
        {localComment.children && localComment.children.length > 0 && (
          <div className="mt-4 space-y-3">
            {localComment.children.map((child) => (
              <CommentItem
                key={child.id}
                comment={child}
                postId={postId}
                currentUserId={currentUserId}
                onDelete={onDelete}
                onReport={onReport}
                onReload={onReload}
                depth={depth + 1}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

