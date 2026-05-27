import EmptyState from '@/components/EmptyState';

interface PlaceholderTabProps {
  module: string;
  feature: string;
}

export default function PlaceholderTab({ module, feature }: PlaceholderTabProps) {
  return (
    <EmptyState
      title={`${feature} arrive in ${module}`}
      description="This tab is wired to the right route already; the implementation lands in the next module."
    />
  );
}
