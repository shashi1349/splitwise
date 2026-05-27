import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { createGroup } from '@/api/groups';
import { getProblemDetail } from '@/api/client';
import Dialog from '@/components/ui/Dialog';
import FormField from '@/components/ui/FormField';
import Button from '@/components/ui/Button';
import Spinner from '@/components/ui/Spinner';

const schema = z.object({
  name: z
    .string()
    .min(1, 'Group name is required')
    .max(100, 'Group name is too long'),
  currencyCode: z
    .string()
    .regex(/^$|^[A-Za-z]{3}$/, 'Use a 3-letter ISO 4217 code, e.g. INR, USD')
    .optional(),
});

type FormValues = z.infer<typeof schema>;

interface CreateGroupDialogProps {
  open: boolean;
  onClose: () => void;
}

export default function CreateGroupDialog({ open, onClose }: CreateGroupDialogProps) {
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const [serverError, setServerError] = useState<string | null>(null);

  const { register, handleSubmit, formState, reset } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { name: '', currencyCode: 'INR' },
  });

  const mutation = useMutation({
    mutationFn: createGroup,
    onSuccess: async (group) => {
      await queryClient.invalidateQueries({ queryKey: ['groups'] });
      queryClient.setQueryData(['group', String(group.id)], group);
      reset();
      onClose();
      navigate(`/groups/${group.id}`);
    },
    onError: (err) => {
      setServerError(getProblemDetail(err).detail ?? 'Could not create group.');
    },
  });

  const onSubmit = handleSubmit((values) => {
    setServerError(null);
    mutation.mutate({
      name: values.name,
      currencyCode: values.currencyCode?.toUpperCase() || undefined,
    });
  });

  const handleClose = () => {
    if (mutation.isPending) return;
    reset();
    setServerError(null);
    onClose();
  };

  return (
    <Dialog
      open={open}
      onClose={handleClose}
      title="Create a group"
      description="Give your group a name and a default currency."
    >
      <form onSubmit={onSubmit} className="flex flex-col gap-4" noValidate>
        <FormField
          label="Group name"
          id="group-name"
          autoComplete="off"
          {...register('name')}
          error={formState.errors.name?.message}
        />
        <FormField
          label="Currency code"
          id="group-currency"
          maxLength={3}
          hint="Defaults to INR if you leave it blank."
          {...register('currencyCode')}
          error={formState.errors.currencyCode?.message}
        />
        {serverError && (
          <p
            role="alert"
            className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-700 dark:bg-red-900/30 dark:text-red-300"
          >
            {serverError}
          </p>
        )}
        <div className="flex items-center justify-end gap-2">
          <Button
            type="button"
            variant="secondary"
            onClick={handleClose}
            disabled={mutation.isPending}
          >
            Cancel
          </Button>
          <Button type="submit" disabled={mutation.isPending}>
            {mutation.isPending ? (
              <>
                <Spinner className="mr-2" />
                Creating
              </>
            ) : (
              'Create group'
            )}
          </Button>
        </div>
      </form>
    </Dialog>
  );
}
