import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { inviteMember } from '@/api/groups';
import { getProblemDetail } from '@/api/client';
import Button from '@/components/ui/Button';
import FormField from '@/components/ui/FormField';
import Spinner from '@/components/ui/Spinner';

const schema = z.object({
  email: z.string().email('Enter a valid email'),
});

type FormValues = z.infer<typeof schema>;

interface InviteMemberFormProps {
  groupId: number;
  onInvited: () => void;
}

export default function InviteMemberForm({ groupId, onInvited }: InviteMemberFormProps) {
  const queryClient = useQueryClient();
  const [feedback, setFeedback] = useState<{ kind: 'ok' | 'error'; text: string } | null>(null);
  const { register, handleSubmit, formState, reset } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { email: '' },
  });

  const mutation = useMutation({
    mutationFn: ({ email }: FormValues) => inviteMember(groupId, email),
    onSuccess: async (member) => {
      setFeedback({
        kind: 'ok',
        text: `${member.displayName} has been added to the group.`,
      });
      reset();
      await queryClient.invalidateQueries({
        queryKey: ['group', String(groupId)],
      });
      await queryClient.invalidateQueries({
        queryKey: ['group-members', groupId],
      });
      onInvited();
    },
    onError: (err) => {
      setFeedback({
        kind: 'error',
        text: getProblemDetail(err).detail ?? 'Could not invite that user.',
      });
    },
  });

  const onSubmit = handleSubmit((values) => {
    setFeedback(null);
    mutation.mutate(values);
  });

  return (
    <form onSubmit={onSubmit} className="flex flex-col gap-3 sm:flex-row sm:items-end" noValidate>
      <div className="flex-1">
        <FormField
          label="Invite by email"
          id="invite-email"
          type="email"
          autoComplete="email"
          placeholder="friend@example.com"
          hint="The user must already be registered."
          {...register('email')}
          error={formState.errors.email?.message}
        />
      </div>
      <Button type="submit" disabled={mutation.isPending}>
        {mutation.isPending ? (
          <>
            <Spinner className="mr-2" />
            Adding
          </>
        ) : (
          'Add member'
        )}
      </Button>
      {feedback && (
        <p
          role="status"
          className={
            feedback.kind === 'ok'
              ? 'rounded-md bg-emerald-50 px-3 py-2 text-sm text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-300 sm:basis-full'
              : 'rounded-md bg-red-50 px-3 py-2 text-sm text-red-700 dark:bg-red-900/30 dark:text-red-300 sm:basis-full'
          }
        >
          {feedback.text}
        </p>
      )}
    </form>
  );
}
