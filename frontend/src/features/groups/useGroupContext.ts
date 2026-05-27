import { useOutletContext } from 'react-router-dom';
import type { GroupDetail } from '@/api/groups';

export interface GroupContext {
  group: GroupDetail;
  refetch: () => void;
}

/** Typed accessor for the group passed via React Router's Outlet context. */
export function useGroupContext(): GroupContext {
  return useOutletContext<GroupContext>();
}
