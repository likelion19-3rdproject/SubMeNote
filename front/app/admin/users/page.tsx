'use client';

import { useEffect, useState, useCallback } from 'react';
import { adminApi } from '@/src/api/adminApi';
import { UserResponseDto } from '@/src/types/user';
import { Page } from '@/src/types/common';
import Card from '@/src/components/common/Card';
import LoadingSpinner from '@/src/components/common/LoadingSpinner';
import ErrorState from '@/src/components/common/ErrorState';
import Pagination from '@/src/components/common/Pagination';

export default function AdminUsersPage() {
  const [users, setUsers] = useState<Page<UserResponseDto> | null>(null);
  const [userCount, setUserCount] = useState<number>(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadData = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      
      // 유저 수와 목록을 동시에 가져옴
      const [countData, listData] = await Promise.all([
        adminApi.getUserCount(),
        adminApi.getUserList(currentPage, 20),
      ]);
      
      setUserCount(countData);
      setUsers(listData);
    } catch (err: any) {
      setError(err.response?.data?.message || '데이터를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  }, [currentPage]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const getRoleDisplay = (roles: string[]) => {
    const roleLabels: { [key: string]: string } = {
      'ROLE_ADMIN': '관리자',
      'ROLE_CREATOR': '크리에이터',
      'ROLE_USER': '사용자',
    };
    
    return roles.map(role => roleLabels[role] || role).join(', ');
  };

  const getRoleBadgeColor = (roles: string[]) => {
    if (roles.includes('ROLE_ADMIN')) {
      return 'bg-red-500/20 text-red-400 border border-red-500/30';
    } else if (roles.includes('ROLE_CREATOR')) {
      return 'bg-purple-500/20 text-purple-400 border border-purple-500/30';
    } else {
      return 'bg-blue-500/20 text-blue-400 border border-blue-500/30';
    }
  };

  if (loading) {
    return (
      <div className="max-w-6xl mx-auto px-6 py-12">
        <LoadingSpinner />
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-6xl mx-auto px-6 py-12">
        <ErrorState message={error} onRetry={loadData} />
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto px-6 py-12 animate-fade-in-scale">
      <div className="mb-8">
        <h1 className="text-4xl font-black text-white mb-10"><span>👥</span> <span className="gradient-text">유저 관리</span></h1>
        <div className="glass border border-purple-400/20 rounded-lg p-4">
          <p className="text-lg text-gray-200">
            전체 유저 수: <span className="font-bold text-purple-400">{userCount}명</span>
          </p>
        </div>
      </div>

      {users && users.content.length > 0 ? (
        <>
          <div className="space-y-4">
            {users.content.map((user) => (
              <Card key={user.id}>
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-4">
                    <div className="w-12 h-12 bg-gradient-to-br from-purple-500 to-purple-600 rounded-full flex items-center justify-center flex-shrink-0">
                      <span className="text-xl font-bold text-white">
                        {user.nickname.charAt(0).toUpperCase()}
                      </span>
                    </div>
                    <div className="flex-1 min-w-0">
                      <h3 className="text-lg font-semibold text-white truncate">
                        {user.nickname}
                      </h3>
                      <p className="text-sm text-gray-400 truncate">
                        {user.email}
                      </p>
                    </div>
                  </div>
                  
                  <div className="flex-shrink-0">
                    <span
                      className={`inline-block px-4 py-2 rounded-full text-sm font-medium ${getRoleBadgeColor(user.roles)}`}
                    >
                      {getRoleDisplay(user.roles)}
                    </span>
                  </div>
                </div>
              </Card>
            ))}
          </div>

          {users.totalPages > 1 && (
            <div className="mt-8">
              <Pagination
                currentPage={currentPage}
                totalPages={users.totalPages}
                onPageChange={setCurrentPage}
              />
            </div>
          )}
        </>
      ) : (
        <Card>
          <div className="text-center py-8">
            <p className="text-gray-400">등록된 유저가 없습니다.</p>
          </div>
        </Card>
      )}
    </div>
  );
}
