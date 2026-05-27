import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { loginUser } from '@/api/auth';
import { getProblemDetail } from '@/api/client';
import { useAuthStore } from '@/store/authStore';
import AuthLayout from './AuthLayout';
import Button from '@/components/ui/Button';
import FormField from '@/components/ui/FormField';
import Spinner from '@/components/ui/Spinner';

const schema = z.object({
  email: z.string().email('Enter a valid email'),
  password: z.string().min(1, 'Password is required'),
});

type FormValues = z.infer<typeof schema>;

interface LocationState {
  from?: { pathname?: string };
}

export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const setAuth = useAuthStore((s) => s.setAuth);
  const [serverError, setServerError] = useState<string | null>(null);
  const { register, handleSubmit, formState } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { email: '', password: '' },
  });

  const from = (location.state as LocationState | null)?.from?.pathname ?? '/';

  const onSubmit = handleSubmit(async (values) => {
    setServerError(null);
    try {
      const result = await loginUser(values);
      setAuth(result.token, result.user);
      navigate(from, { replace: true });
    } catch (err) {
      const problem = getProblemDetail(err);
      setServerError(problem.detail ?? 'Login failed.');
    }
  });

  return (
    <AuthLayout
      title="Welcome back"
      subtitle="Sign in to keep splitting expenses with friends."
      footer={
        <p className="text-sm text-slate-600 dark:text-slate-400">
          Need an account?{' '}
          <Link
            to="/register"
            className="font-medium text-brand-600 hover:underline"
          >
            Create one
          </Link>
        </p>
      }
    >
      <form onSubmit={onSubmit} className="flex flex-col gap-4" noValidate>
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
          autoComplete="current-password"
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
              Signing in
            </>
          ) : (
            'Sign in'
          )}
        </Button>
      </form>
    </AuthLayout>
  );
}
