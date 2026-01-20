"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { postApi } from "@/src/api/postApi";
import Input from "@/src/components/common/Input";
import Textarea from "@/src/components/common/Textarea";
import Button from "@/src/components/common/Button";
import ErrorState from "@/src/components/common/ErrorState";

export default function NewPostPage() {
  const router = useRouter();
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [visibility, setVisibility] = useState<"PUBLIC" | "SUBSCRIBERS_ONLY">(
    "PUBLIC"
  );
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const post = await postApi.createPost({ title, content, visibility });
      router.push(`/posts/${post.id}`);
    } catch (err: any) {
      setError(err.response?.data?.message || "게시글 작성에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto px-6 py-16">
      <h1 className="text-3xl font-bold text-gray-900 mb-12">게시글 작성</h1>

      {error && <ErrorState message={error} />}

      <Card className="p-8">
        <form onSubmit={handleSubmit} className="space-y-8">
          <Input
            label="제목"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            required
            disabled={loading}
            className="text-xl"
          />

          <Textarea
            label="내용"
            value={content}
            onChange={(e) => setContent(e.target.value)}
            required
            rows={20}
            disabled={loading}
            className="leading-relaxed"
          />

          <div>
            <label className="block text-sm font-semibold text-gray-900 mb-4">
              공개 범위
            </label>
            <div className="space-y-3">
              <label className="flex items-center cursor-pointer p-3 rounded-xl hover:bg-gray-50 transition-colors">
                <input
                  type="radio"
                  value="PUBLIC"
                  checked={visibility === "PUBLIC"}
                  onChange={(e) =>
                    setVisibility(e.target.value as "PUBLIC" | "SUBSCRIBERS_ONLY")
                  }
                  className="mr-3 w-4 h-4 text-[#FFC837]"
                />
                <span className="text-gray-900 font-medium">전체공개</span>
              </label>
              <label className="flex items-center cursor-pointer p-3 rounded-xl hover:bg-gray-50 transition-colors">
                <input
                  type="radio"
                  value="SUBSCRIBERS_ONLY"
                  checked={visibility === "SUBSCRIBERS_ONLY"}
                  onChange={(e) =>
                    setVisibility(e.target.value as "PUBLIC" | "SUBSCRIBERS_ONLY")
                  }
                  className="mr-3 w-4 h-4 text-[#FFC837]"
                />
                <span className="text-gray-900 font-medium">멤버십전용</span>
              </label>
            </div>
          </div>

          <div className="flex gap-3 pt-4 border-t border-gray-200">
            <Button type="submit" disabled={loading}>
              {loading ? "작성 중..." : "작성하기"}
            </Button>
            <Button
              type="button"
              variant="secondary"
              onClick={() => router.back()}
              disabled={loading}
            >
              취소
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
}
