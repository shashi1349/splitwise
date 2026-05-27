import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { registerUser } from '@/api/auth';
import { getProblemDetail } from '@/api/client';
import { useAuthStore } from '@/store/authStore';
import AuthLayout from './AuthLayout';
import Button from '@/components/ui/Button';
import FormField from '@/components/ui/FormField';
import Spinner from '@/components/ui/Spinner';

const schema = z.object({
  email: z.string().email('Enter a valid email'),
  displayName: z
    .string()
    .min(1, 'Display name is required')
    .max(100, 'Display name is too long'),
  password: z.string().min(8, 'Use at least 8 characters'),
});

type FormValues = z.infer<typeof schema>;

export default function RegisterPage() {
  const navigate = useNavigate();
  const setAuth = useAuthStore((s) => s.setAuth);
  const [serverError, setServerError] = useState<string | null>(null);
  const { register, handleSubmit, formState } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { email: '', displayName: '', password: '' },
  });

  const onSubmit = handleSubmit(async (values) => {
    setServerError(null);
    try {
      const result = await registerUser(values);
      setAuth(result.token, result.user);
      navigate('/', { replace: true });
    } catch (err) {
      const problem = getProblemDetail(err);
      setServerError(problem.detail ?? 'Could not create account.');
    }
  });

  return (
    <AuthLayout
      title="Create your account"
      subtitle="Track expenses with friends and settle up in the fewest possible transfers."
      footer={
        <p className="text-sm text-slate-600 dark:text-slate-400">
          Already have an account?{' '}
          <Link to="/login" className="font-medium text-brand-600 hover:underline">
            Sign in
          </Link>
        </p>
      }
    >
      <form onSubmit={onSubmit} className="flex flex-col gap-4" noValidate>
        <FormField
          label="Display name"
          id="displayName"
          autoComplete="name"
          {...register('displayName')}
          error={formState.errors.displayName?.message}
        />
        <FormField
          label="Email"
          id="email"
          type="email"
          autoComplete="email"
          {...register('email')}
          error={formState.errors.email?.message}
        />
        <FormField
          label="Password"
          id="password"
          type="password"
          autoComplete="new-password"
          hint="At least 8 characters."
          {...register('password')}
          error={formState.errors.password?.message}
        />
        {serverError && (
          <p
            role="alert"
            className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-700 dark:bg-red-900/30 dark:text-red-300"
          >
            {serverError}
          </p>
        )}
        <Button
          type="submit"
          disabled={formState.isSubmitting}
          className="w-full"
        >
          {formState.isSubmitting ? (
            <>
              <Spinner className="mr-2" />
              Creating account
            </>
          ) : (
            'Create account'
          )}
        </Button>
      </form>
    </AuthLayout>
  );
}
