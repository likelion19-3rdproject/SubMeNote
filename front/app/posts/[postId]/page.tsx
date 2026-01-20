'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { postApi } from '@/src/api/postApi';
import { commentApi } from '@/src/api/commentApi';
import { userApi } from '@/src/api/userApi';
import { likeApi } from '@/src/api/likeApi';
import { PostResponseDto } from '@/src/types/post';
import { CommentResponseDto } from '@/src/types/comment';
import { Page } from '@/src/types/common';
import Card from '@/src/components/common/Card';
import LoadingSpinner from '@/src/components/common/LoadingSpinner';
import ErrorState from '@/src/components/common/ErrorState';
import Button from '@/src/components/common/Button';
import Input from '@/src/components/common/Input';
import Textarea from '@/src/components/common/Textarea';
import ReportModal from '@/src/components/report/ReportModal';
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
  const [showReportModal, setShowReportModal] = useState(false);
  const [reportTarget, setReportTarget] = useState<{ id: number; type: 'POST' | 'COMMENT' } | null>(null);
  const [isEditingPost, setIsEditingPost] = useState(false);
  const [editTitle, setEditTitle] = useState('');
  const [editContent, setEditContent] = useState('');
  const [editVisibility, setEditVisibility] = useState<'PUBLIC' | 'SUBSCRIBERS_ONLY'>('PUBLIC');

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

  const handleReportComment = (commentId: number) => {
    setReportTarget({ id: commentId, type: 'COMMENT' });
    setShowReportModal(true);
  };

  const handleTogglePostLike = async () => {
    if (!post) return;

    try {
      const result = await likeApi.togglePostLike(postId);
      // 게시글 상태 업데이트
      setPost({
        ...post,
        likeCount: result.likeCount,
        likedByMe: result.liked,
      });
    } catch (err: any) {
      alert(err.response?.data?.message || '좋아요 처리에 실패했습니다.');
    }
  };

  const handleEditPost = () => {
    if (!post) return;
    setEditTitle(post.title);
    setEditContent(post.content);
    setEditVisibility(post.visibility);
    setIsEditingPost(true);
  };

  const handleCancelEditPost = () => {
    setIsEditingPost(false);
    setEditTitle('');
    setEditContent('');
    setEditVisibility('PUBLIC');
  };

  const handleSubmitEditPost = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editTitle.trim() || !editContent.trim()) {
      alert('제목과 내용을 입력해주세요.');
      return;
    }

    try {
      setCommentLoading(true);
      const updatedPost = await postApi.updatePost(postId, {
        title: editTitle,
        content: editContent,
        visibility: editVisibility,
      });
      setPost(updatedPost);
      setIsEditingPost(false);
      alert('게시글이 수정되었습니다.');
    } catch (err: any) {
      alert(err.response?.data?.message || '게시글 수정에 실패했습니다.');
    } finally {
      setCommentLoading(false);
    }
  };

  const handleDeletePost = async () => {
    if (!confirm('게시글을 삭제하시겠습니까?')) return;

    try {
      await postApi.deletePost(postId);
      alert('게시글이 삭제되었습니다.');
      router.push('/feed');
    } catch (err: any) {
      alert(err.response?.data?.message || '게시글 삭제에 실패했습니다.');
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
        <div className="glass p-12 text-center rounded-2xl border border-purple-500/20">
          <div className="text-7xl mb-6">❓</div>
          <p className="text-gray-400 text-xl font-bold">게시글을 찾을 수 없습니다.</p>
        </div>
      </div>
    );
  }

  // 본인 게시글인지 확인
  const isMyPost = currentUserId !== null && currentUserId === post.userId;

  return (
    <div className="max-w-3xl mx-auto px-6 py-12">
      <article className="mb-16">
        {isEditingPost ? (
          // 게시글 수정 모드
          <Card>
            <form onSubmit={handleSubmitEditPost} className="space-y-6">
              <div>
                <Input
                  value={editTitle}
                  onChange={(e) => setEditTitle(e.target.value)}
                  placeholder="제목을 입력하세요..."
                  disabled={commentLoading}
                  className="text-2xl font-bold"
                />
              </div>
              <div>
                <label className="block text-sm font-bold text-gray-300 mb-2">
                  공개 범위
                </label>
                <select
                  value={editVisibility}
                  onChange={(e) => setEditVisibility(e.target.value as 'PUBLIC' | 'SUBSCRIBERS_ONLY')}
                  disabled={commentLoading}
                  className="w-full px-4 py-3 glass border-2 border-purple-500/30 rounded-xl focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-purple-500/60 text-white transition-all duration-300"
                >
                  <option value="PUBLIC" className="bg-gray-800 text-white">전체 공개</option>
                  <option value="SUBSCRIBERS_ONLY" className="bg-gray-800 text-white">구독자만</option>
                </select>
              </div>
              <div className="flex items-center gap-3 py-4 relative">
                {/* 그라데이션 구분선 (상단) */}
                <div className="absolute top-0 left-0 right-0 h-px bg-gradient-to-r from-transparent via-purple-400/40 to-transparent"></div>
                <div className="absolute top-0 left-0 right-0 h-[1px] bg-purple-400/20 blur-sm"></div>
                {/* 그라데이션 구분선 (하단) */}
                <div className="absolute bottom-0 left-0 right-0 h-px bg-gradient-to-r from-transparent via-purple-400/40 to-transparent"></div>
                <div className="absolute bottom-0 left-0 right-0 h-[1px] bg-purple-400/20 blur-sm"></div>
                <div className="w-12 h-12 rounded-full bg-gradient-to-r from-purple-500 to-purple-600 flex items-center justify-center text-white font-black neon-glow">
                  {post.nickname.charAt(0).toUpperCase()}
                </div>
                <div>
                  <p className="font-bold text-white">{post.nickname}</p>
                  <p className="text-sm text-gray-500">
                    {new Date(post.createdAt).toLocaleDateString("ko-KR", {
                      year: "numeric",
                      month: "long",
                      day: "numeric",
                    })}
                  </p>
                </div>
              </div>
              <Textarea
                value={editContent}
                onChange={(e) => setEditContent(e.target.value)}
                placeholder="내용을 입력하세요..."
                rows={15}
                disabled={commentLoading}
              />
              <div className="flex gap-2">
                <Button
                  type="submit"
                  disabled={commentLoading || !editTitle.trim() || !editContent.trim()}
                >
                  {commentLoading ? '💾 저장 중...' : '💾 저장'}
                </Button>
                <Button
                  type="button"
                  variant="secondary"
                  onClick={handleCancelEditPost}
                  disabled={commentLoading}
                >
                  ❌ 취소
                </Button>
              </div>
            </form>
          </Card>
        ) : (
          // 게시글 읽기 모드
          <Card>
            <div className="flex justify-between items-start mb-6">
              <h1 className="text-4xl font-bold text-white leading-tight flex-1">
                {post.title}
              </h1>
              {isMyPost ? (
                <div className="flex gap-2 ml-4">
                  <Button
                    variant="secondary"
                    size="sm"
                    onClick={handleEditPost}
                  >
                    ✏️ 수정
                  </Button>
                  <Button
                    variant="danger"
                    size="sm"
                    onClick={handleDeletePost}
                  >
                    🗑️ 삭제
                  </Button>
                </div>
              ) : (
                <Button
                  variant="secondary"
                  size="sm"
                  onClick={() => {
                    setReportTarget({ id: postId, type: 'POST' });
                    setShowReportModal(true);
                  }}
                  className="ml-4"
                >
                  🚨 신고
                </Button>
              )}
            </div>
            <div className="flex items-center gap-3 mb-8 pb-6 relative">
              {/* 그라데이션 구분선 */}
              <div className="absolute bottom-0 left-0 right-0 h-[2px] bg-gradient-to-r from-transparent via-purple-400/50 to-transparent"></div>
              <div className="absolute bottom-0 left-0 right-0 h-[2px] bg-purple-400/30 blur-sm"></div>
              <div className="w-12 h-12 rounded-full bg-gradient-to-r from-purple-500 to-purple-600 flex items-center justify-center text-white font-bold text-lg neon-glow">
                {post.nickname.charAt(0).toUpperCase()}
              </div>
              <div>
                <p className="font-semibold text-white text-lg">{post.nickname}</p>
                <p className="text-sm text-gray-500">
                  {new Date(post.createdAt).toLocaleDateString("ko-KR", {
                    year: "numeric",
                    month: "long",
                    day: "numeric",
                  })}
                </p>
              </div>
            </div>
            <div className="prose max-w-none">
              <div className="text-gray-300 whitespace-pre-wrap leading-relaxed text-lg">
                {post.content}
              </div>
            </div>
            
            {/* 좋아요 버튼 */}
            <div className="mt-8 pt-6 relative">
              {/* 그라데이션 구분선 */}
              <div className="absolute top-0 left-0 right-0 h-[2px] bg-gradient-to-r from-transparent via-purple-400/50 to-transparent"></div>
              <div className="absolute top-0 left-0 right-0 h-[2px] bg-purple-400/30 blur-sm"></div>
              <Button
                variant={post.likedByMe ? 'primary' : 'secondary'}
                onClick={handleTogglePostLike}
                className="flex items-center gap-2"
              >
                <span className="text-xl">{post.likedByMe ? '❤️' : '🤍'}</span>
                <span>좋아요 {post.likeCount}</span>
              </Button>
            </div>
          </Card>
        )}
      </article>

      {/* 댓글 작성 */}
      <Card className="mb-8">
        <div className="text-white">
          <h2 className="text-2xl font-bold mb-4 gradient-text">
            💬 댓글 작성
          </h2>
          <form onSubmit={handleSubmitComment}>
            <Textarea
              value={newComment}
              onChange={(e) => setNewComment(e.target.value)}
              placeholder="댓글을 입력하세요..."
              rows={4}
              disabled={commentLoading}
              className="mb-4"
            />
            <Button type="submit" disabled={commentLoading || !newComment.trim()}>
              {commentLoading ? "작성 중..." : "✍️ 댓글 작성"}
            </Button>
          </form>
        </div>
      </Card>

      {/* 댓글 목록 */}
      <div>
        <h2 className="text-2xl font-bold mb-6 gradient-text">
          💬 댓글 ({comments?.totalElements || 0})
        </h2>
        {comments && comments.content.length > 0 ? (
          <div className="space-y-4">
            {comments.content.map((comment) => (
              <CommentItem
                key={comment.id}
                comment={comment}
                postId={postId}
                currentUserId={currentUserId}
                onDelete={handleDeleteComment}
                onReport={handleReportComment}
                onReload={loadData}
              />
            ))}
          </div>
        ) : (
          <div className="glass p-12 text-center rounded-2xl">
            <div className="text-6xl mb-4">💭</div>
            <p className="text-gray-300 text-lg">댓글이 없습니다.</p>
          </div>
        )}
      </div>

      {/* 신고 모달 */}
      {showReportModal && reportTarget && (
        <ReportModal
          targetId={reportTarget.id}
          type={reportTarget.type}
          onClose={() => {
            setShowReportModal(false);
            setReportTarget(null);
          }}
          onSuccess={() => {
            // 필요시 페이지 새로고침
          }}
        />
      )}
    </div>
  );
}

