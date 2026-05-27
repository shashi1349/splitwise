import { describe, expect, it } from 'vitest';
import { screen } from '@testing-library/react';
import { Outlet, Route, Routes } from 'react-router-dom';
import { http, HttpResponse } from 'msw';
import { renderWithProviders } from '@/test/utils';
import { server } from '@/test/server';
import BalancesTab from './BalancesTab';
import type { GroupContext } from '../groups/useGroupContext';
import type { GroupDetail } from '@/api/groups';

const fakeGroup: GroupDetail = {
  id: 1,
  name: 'Trip',
  currencyCode: 'INR',
  createdById: 1,
  createdAt: new Date().toISOString(),
  members: [
    {
      userId: 1,
      email: 'alice@example.com',
      displayName: 'Alice',
      role: 'OWNER',
      joinedAt: new Date().toISOString(),
    },
    {
      userId: 2,
      email: 'bob@example.com',
      displayName: 'Bob',
      role: 'MEMBER',
      joinedAt: new Date().toISOString(),
    },
  ],
};

const ctx: GroupContext = { group: fakeGroup, refetch: () => {} };

function ContextLayout() {
  return <Outlet context={ctx} />;
}

function TestRoutes() {
  return (
    <Routes>
      <Route element={<ContextLayout />}>
        <Route path="/" element={<BalancesTab />} />
      </Route>
    </Routes>
  );
}

describe('BalancesTab', () => {
  it('renders English status lines with creditor and debtor amounts', async () => {
    server.use(
      http.get('http://localhost:8080/groups/1/balances', () =>
        HttpResponse.json([
          { userId: 1, displayName: 'Alice', netCents: 18000, currencyCode: 'INR' },
          { userId: 2, displayName: 'Bob', netCents: -18000, currencyCode: 'INR' },
        ]),
      ),
    );
    renderWithProviders(<TestRoutes />);
    expect(await screen.findByText(/Alice is owed/)).toBeInTheDocument();
    expect(await screen.findByText(/Bob owes/)).toBeInTheDocument();
  });

  it('shows the settled-up message when every balance is zero', async () => {
    server.use(
      http.get('http://localhost:8080/groups/1/balances', () =>
        HttpResponse.json([
          { userId: 1, displayName: 'Alice', netCents: 0, currencyCode: 'INR' },
          { userId: 2, displayName: 'Bob', netCents: 0, currencyCode: 'INR' },
        ]),
      ),
    );
    renderWithProviders(<TestRoutes />);
    expect(await screen.findByText(/Everyone is settled up/i)).toBeInTheDocument();
  });

  it('renders an error banner when the backend returns 500', async () => {
    server.use(
      http.get('http://localhost:8080/groups/1/balances', () =>
        HttpResponse.json(
          { detail: 'kaboom' },
          { status: 500 },
        ),
      ),
    );
    renderWithProviders(<TestRoutes />);
    expect(await screen.findByRole('alert')).toHaveTextContent(/Could not load balances/i);
  });
});
