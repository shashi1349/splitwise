import { Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from '@/auth/LoginPage';
import RegisterPage from '@/auth/RegisterPage';
import RequireAuth from '@/auth/RequireAuth';
import AppShell from '@/components/AppShell';
import GroupsPage from '@/features/groups/GroupsPage';
import GroupDetailPage from '@/features/groups/GroupDetailPage';
import MembersTab from '@/features/groups/MembersTab';
import PlaceholderTab from '@/features/groups/PlaceholderTab';
import ExpensesTab from '@/features/expenses/ExpensesTab';

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route element={<RequireAuth />}>
        <Route element={<AppShell />}>
          <Route path="/" element={<Navigate to="/groups" replace />} />
          <Route path="/groups" element={<GroupsPage />} />
          <Route path="/groups/:groupId" element={<GroupDetailPage />}>
            <Route index element={<Navigate to="expenses" replace />} />
            <Route path="expenses" element={<ExpensesTab />} />
            <Route
              path="balances"
              element={<PlaceholderTab module="Module 5" feature="Balances" />}
            />
            <Route
              path="settle"
              element={<PlaceholderTab module="Module 6" feature="Settle-up suggestions" />}
            />
            <Route path="members" element={<MembersTab />} />
          </Route>
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
