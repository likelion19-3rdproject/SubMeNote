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
      return 'bg-red-100 text-red-800';
    } else if (roles.includes('ROLE_CREATOR')) {
      return 'bg-purple-100 text-purple-800';
    } else {
      return 'bg-blue-100 text-blue-800';
    }
  };

  if (loading) {
    return (
      <div className="max-w-7xl mx-auto px-6 py-16">
        <LoadingSpinner />
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-7xl mx-auto px-6 py-16">
        <ErrorState message={error} onRetry={loadData} />
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-6 py-16">
      <div className="mb-12">
        <h1 className="text-3xl font-bold text-gray-900 mb-6">유저 관리</h1>
        <Card className="bg-[#FFF4D6] border-[#FFC837]">
          <p className="text-lg text-gray-900">
            전체 유저 수: <span className="font-bold text-[#FF9500]">{userCount}명</span>
          </p>
        </Card>
      </div>

      {users && users.content.length > 0 ? (
        <>
          <div className="grid gap-4">
            {users.content.map((user) => (
              <Card key={user.id}>
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-4">
                    <div className="w-12 h-12 bg-gradient-to-br from-[#FFC837] to-[#FF9500] rounded-full flex items-center justify-center flex-shrink-0 shadow-sm">
                      <span className="text-xl font-bold text-white">
                        {user.nickname.charAt(0).toUpperCase()}
                      </span>
                    </div>
                    <div className="flex-1 min-w-0">
                      <h3 className="text-lg font-semibold text-gray-900 truncate">
                        {user.nickname}
                      </h3>
                      <p className="text-sm text-gray-500 truncate">
                        {user.email}
                      </p>
                    </div>
                  </div>
                  
                  <div className="flex-shrink-0">
                    <span
                      className={`inline-block px-4 py-2 rounded-xl text-sm font-medium ${getRoleBadgeColor(user.roles)}`}
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
        <div className="bg-gray-50 border border-gray-200 rounded-xl p-12 text-center">
          <p className="text-gray-500">등록된 유저가 없습니다.</p>
        </div>
      )}
    </div>
  );
}
