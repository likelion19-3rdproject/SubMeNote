'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { postApi } from '@/src/api/postApi';
import { commentApi } from '@/src/api/commentApi';
import { userApi } from '@/src/api/userApi';
import { PostResponseDto } from '@/src/types/post';
import { CommentResponseDto } from '@/src/types/comment';
import { Page } from '@/src/types/common';
import Card from '@/src/components/common/Card';
import LoadingSpinner from '@/src/components/common/LoadingSpinner';
import ErrorState from '@/src/components/common/ErrorState';
import Button from '@/src/components/common/Button';
import Textarea from '@/src/components/common/Textarea';
import CommentItem from '@/src/components/comment/CommentItem';

export default function PostDetailPage() {
  const params = useParams();
  const router = useRouter();
  const postId = Number(params.postId);
  const [post, setPost] = useState<PostResponseDto | null>(null);
  const [comments, setComments] = useState<Page<CommentResponseDto> | null>(null);
  const [newComment, setNewComment] = useState('');
  const [currentUserId, setCurrentUserId] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [commentLoading, setCommentLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!postId) return;

    let isMounted = true;

    const loadDataWithMountCheck = async () => {
      try {
        setLoading(true);
        setError(null);
        
        // 현재 로그인한 사용자 정보 가져오기
        let userId: number | null = null;
        try {
          const user = await userApi.getMe();
          userId = user.id;
        } catch (err) {
          // 로그인 안 된 경우 null 유지
        }
        
        const [postData, commentsData] = await Promise.all([
          postApi.getPost(postId),
          commentApi.getComments(postId),
        ]);
        
        if (isMounted) {
          setPost(postData);
          setComments(commentsData);
          setCurrentUserId(userId);
        }
      } catch (err: any) {
        if (!isMounted) return;
        
        // 403 에러 처리 (구독 필요 또는 멤버십 필요)
        if (err.response?.status === 403) {
          const errorMessage = err.response?.data?.message || '이 게시글에 접근할 권한이 없습니다.';
          if (errorMessage.includes('구독') || errorMessage.includes('멤버십')) {
            setError(`${errorMessage} 크리에이터 페이지에서 구독 또는 멤버십 가입을 해주세요.`);
          } else {
            setError(errorMessage);
          }
        } else {
          setError(err.response?.data?.message || '데이터를 불러오는데 실패했습니다.');
        }
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    };

    loadDataWithMountCheck();

    return () => {
      isMounted = false;
    };
  }, [postId]);

  const loadData = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // 현재 로그인한 사용자 정보 가져오기
      let userId: number | null = null;
      try {
        const user = await userApi.getMe();
        userId = user.id;
      } catch (err) {
        // 로그인 안 된 경우 null 유지
      }
      
      const [postData, commentsData] = await Promise.all([
        postApi.getPost(postId),
        commentApi.getComments(postId),
      ]);

      setPost(postData);
      setComments(commentsData);
      setCurrentUserId(userId);
    } catch (err: any) {
      // 403 에러 처리 (구독 필요 또는 멤버십 필요)
      if (err.response?.status === 403) {
        const errorMessage =
          err.response?.data?.message || "이 게시글에 접근할 권한이 없습니다.";
        if (errorMessage.includes("구독") || errorMessage.includes("멤버십")) {
          setError(
            `${errorMessage} 크리에이터 페이지에서 구독 또는 멤버십 가입을 해주세요.`
          );
        } else {
          setError(errorMessage);
        }
      } else {
        setError(
          err.response?.data?.message || "데이터를 불러오는데 실패했습니다."
        );
      }
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitComment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newComment.trim()) return;

    try {
      setCommentLoading(true);
      await commentApi.createComment(postId, { content: newComment });
      setNewComment('');
      loadData(); // 댓글 목록 새로고침
    } catch (err: any) {
      alert(err.response?.data?.message || '댓글 작성에 실패했습니다.');
    } finally {
      setCommentLoading(false);
    }
  };


  const handleDeleteComment = async (commentId: number) => {
    if (!confirm('댓글을 삭제하시겠습니까?')) return;

    try {
      await commentApi.deleteComment(commentId);
      loadData(); // 댓글 목록 새로고침
    } catch (err: any) {
      alert(err.response?.data?.message || '댓글 삭제에 실패했습니다.');
    }
  };

  if (loading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <LoadingSpinner />
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <ErrorState message={error} onRetry={loadData} />
      </div>
    );
  }

  if (!post) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <p className="text-gray-500">게시글을 찾을 수 없습니다.</p>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto px-6 py-12">
      <article className="mb-16">
        <h1 className="text-4xl font-normal text-gray-900 mb-6 leading-tight">
          {post.title}
        </h1>
        <div className="flex justify-between items-center text-sm text-gray-500 mb-8 pb-8 border-b border-gray-100">
          <span className="font-normal">{post.nickname}</span>
          <span className="font-normal">
            {new Date(post.createdAt).toLocaleDateString("ko-KR", {
              year: "numeric",
              month: "long",
              day: "numeric",
            })}
          </span>
        </div>
        <div className="prose max-w-none">
          <div className="text-gray-700 whitespace-pre-wrap leading-relaxed text-base">
            {post.content}
          </div>
        </div>
      </article>

      {/* 댓글 작성 */}
      <div className="text-gray-900 mb-12 pb-8 border-b border-gray-100">
        <h2 className="text-sm font-normal text-gray-500 mb-6 uppercase tracking-wider">
          댓글 작성
        </h2>
        <form onSubmit={handleSubmitComment}>
          <Textarea
            value={newComment}
            onChange={(e) => setNewComment(e.target.value)}
            placeholder="댓글을 입력하세요..."
            rows={4}
            disabled={commentLoading}
            className="mb-4 border-gray-200 focus:border-gray-400 rounded-sm"
          />
          <Button type="submit" disabled={commentLoading || !newComment.trim()}>
            {commentLoading ? "작성 중..." : "댓글 작성"}
          </Button>
        </form>
      </div>

      {/* 댓글 목록 */}
      <div>
        <h2 className="text-sm font-normal text-gray-500 mb-6 uppercase tracking-wider">
          댓글 ({comments?.totalElements || 0})
        </h2>
        {comments && comments.content.length > 0 ? (
          <div className="space-y-0 border-t border-gray-100">
            {comments.content.map((comment) => (
              <CommentItem
                key={comment.id}
                comment={comment}
                postId={postId}
                currentUserId={currentUserId}
                onDelete={handleDeleteComment}
                onReload={loadData}
              />
            ))}
          </div>
        ) : (
          <p className="text-gray-500 py-8">댓글이 없습니다.</p>
        )}
      </div>
    </div>
  );
}

