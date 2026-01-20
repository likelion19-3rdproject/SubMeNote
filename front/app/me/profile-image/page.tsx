'use client';

import { useState, useEffect } from 'react';
import { profileImageApi } from '@/src/api/profileImageApi';
import { userApi } from '@/src/api/userApi';
import Card from '@/src/components/common/Card';
import Button from '@/src/components/common/Button';
import LoadingSpinner from '@/src/components/common/LoadingSpinner';
import CreatorProfileImage from '@/src/components/common/CreatorProfileImage';

export default function ProfileImagePage() {
  const [userId, setUserId] = useState<number | null>(null);
  const [userNickname, setUserNickname] = useState<string>('');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [preview, setPreview] = useState<string | null>(null);
  const [uploading, setUploading] = useState(false);
  const [loading, setLoading] = useState(true);
  const [currentImageUrl, setCurrentImageUrl] = useState<string | null>(null);
  const [imageKey, setImageKey] = useState<number>(0);

  useEffect(() => {
    const fetchUserInfo = async () => {
      try {
        const user = await userApi.getMe();
        setUserId(user.id);
        setUserNickname(user.nickname);
        setCurrentImageUrl(profileImageApi.getProfileImageUrl(user.id));
      } catch (error) {
        console.error('Failed to fetch user info:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchUserInfo();
  }, []);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setSelectedFile(file);
      const reader = new FileReader();
      reader.onloadend = () => {
        setPreview(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleUpload = async () => {
    if (!selectedFile) {
      alert('파일을 선택해주세요.');
      return;
    }

    try {
      setUploading(true);
      await profileImageApi.uploadProfileImage(selectedFile);
      alert('프로필 이미지가 업로드되었습니다.');
      setSelectedFile(null);
      setPreview(null);
      // 이미지 재로드 - imageKey를 변경하여 컴포넌트 강제 리렌더링
      if (userId) {
        setImageKey(prev => prev + 1);
        setCurrentImageUrl(profileImageApi.getProfileImageUrl(userId) + `?t=${Date.now()}`);
      }
    } catch (err: any) {
      alert(err.response?.data?.message || '이미지 업로드에 실패했습니다.');
    } finally {
      setUploading(false);
    }
  };

  if (loading) {
    return (
      <div className="max-w-4xl mx-auto px-6 py-12">
        <LoadingSpinner />
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto px-6 py-12 animate-fade-in-scale">
      <h1 className="text-4xl font-black text-white mb-10"><span>🖼️</span> <span className="gradient-text">프로필 이미지 설정</span></h1>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* 현재 프로필 이미지 */}
        <Card>
          <h3 className="text-lg font-semibold text-white mb-4">현재 프로필 이미지</h3>
          <div className="flex justify-center" key={imageKey}>
            {userId && (
              <div className="border-4 border-purple-400/25 rounded-full">
                <CreatorProfileImage
                  creatorId={userId}
                  nickname={userNickname}
                  size="xl"
                />
              </div>
            )}
          </div>
        </Card>

        {/* 새 이미지 업로드 */}
        <Card>
          <h3 className="text-lg font-semibold text-white mb-4">새 이미지 업로드</h3>
          <div className="space-y-4">
            {preview && (
              <div className="flex justify-center">
                <img
                  src={preview}
                  alt="Preview"
                  className="w-48 h-48 rounded-full object-cover border-4 border-purple-400/25"
                />
              </div>
            )}

            <div>
              <label
                htmlFor="file-upload"
                className="flex items-center justify-center w-full px-4 py-3 border-2 border-dashed border-purple-400/30 rounded-lg cursor-pointer hover:border-purple-400/50 transition-colors"
              >
                <div className="text-center">
                  <p className="text-sm text-gray-300">
                    클릭하여 이미지 선택
                  </p>
                  <p className="text-xs text-gray-400 mt-1">
                    JPG, PNG (최대 10MB)
                  </p>
                </div>
                <input
                  id="file-upload"
                  type="file"
                  accept="image/*"
                  onChange={handleFileChange}
                  className="hidden"
                  disabled={uploading}
                />
              </label>
            </div>

            {selectedFile && (
              <div className="text-sm text-gray-300">
                선택된 파일: {selectedFile.name}
              </div>
            )}

            <Button
              onClick={handleUpload}
              disabled={!selectedFile || uploading}
              className="w-full"
            >
              {uploading ? '업로드 중...' : '업로드'}
            </Button>
          </div>
        </Card>
      </div>

      <Card className="mt-6">
        <h3 className="text-lg font-semibold text-white mb-2">안내사항</h3>
        <ul className="text-sm text-gray-300 space-y-1 list-disc list-inside">
          <li>정사각형 이미지를 권장합니다.</li>
          <li>최대 파일 크기는 10MB입니다.</li>
          <li>JPG, PNG 형식을 지원합니다.</li>
          <li>얼굴이 잘 보이는 이미지를 사용해주세요.</li>
        </ul>
      </Card>
    </div>
  );
}
