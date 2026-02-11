const completedBackendFeatures = [
  'User registration with validation and ROLE_USER mapping',
  'Login with lockout after configurable failed attempts',
  'Refresh token rotation flow',
  'JWT access + refresh issuance with token-type checks',
  'Per-IP login rate limiting',
  'Liquibase schema + seed data for users/roles',
  'Tenant service onboarding with generated API keys and tier limits',
  'Admin user management APIs (list/get/update/delete + role assignment)',
  'MFA challenge + OTP verification endpoints for two-step auth',
  'Account recovery via security-question + OTP reset flow',
];

const upcomingFeatures = [
  'OAuth social login providers (Google, Meta, Microsoft)',
  'Security question management UI + full self-service recovery screens',
  'Admin dashboard UI screens for user CRUD and role workflows',
  'Complete API key lifecycle actions (rotate/revoke)',
  'Traffic analytics and alerting views',
];

const apiEndpoints = [
  {
    method: 'POST',
    path: '/api/v1/auth/register',
    status: 'Implemented',
    notes: 'Creates account and returns access/refresh tokens.',
  },
  {
    method: 'POST',
    path: '/api/v1/auth/login',
    status: 'Implemented',
    notes: 'Validates credentials, enforces lockout/rate limits.',
  },
  {
    method: 'POST',
    path: '/api/v1/auth/refresh',
    status: 'Implemented',
    notes: 'Rotates tokens using refresh JWT.',
  },
  {
    method: 'POST',
    path: '/api/v1/services',
    status: 'Implemented',
    notes: 'Onboards a tenant service and generates API key.',
  },
  {
    method: 'GET',
    path: '/api/v1/services',
    status: 'Implemented',
    notes: 'Lists onboarded services with tier and limits.',
  },
  {
    method: 'GET',
    path: '/api/v1/admin/users',
    status: 'Implemented',
    notes: 'Lists users for admin management.',
  },
  {
    method: 'PUT',
    path: '/api/v1/admin/users/{userId}',
    status: 'Implemented',
    notes: 'Updates account status, password, and role mappings.',
  },
  {
    method: 'POST',
    path: '/api/v1/auth/mfa/challenge',
    status: 'Implemented',
    notes: 'Starts MFA challenge after primary credential validation.',
  },
  {
    method: 'POST',
    path: '/api/v1/auth/mfa/verify',
    status: 'Implemented',
    notes: 'Validates OTP and issues JWT access/refresh tokens.',
  },
  {
    method: 'POST',
    path: '/api/v1/auth/recovery/challenge',
    status: 'Implemented',
    notes: 'Validates security answer and sends OTP challenge.',
  },
  {
    method: 'POST',
    path: '/api/v1/auth/recovery/reset',
    status: 'Implemented',
    notes: 'Resets password using challengeId + OTP + new password.',
  },
];

function Badge({ children, tone = 'default' }) {
  const styles = {
    success: 'bg-emerald-500/15 text-emerald-300 border-emerald-500/30',
    default: 'bg-slate-800 text-slate-300 border-slate-700',
    info: 'bg-cyan-500/15 text-cyan-300 border-cyan-500/30',
  };

  return <span className={`rounded-full border px-2 py-1 text-xs font-medium ${styles[tone]}`}>{children}</span>;
}

function SectionCard({ title, subtitle, children }) {
  return (
    <section className="rounded-xl border border-slate-800 bg-slate-900/70 p-5 shadow-lg shadow-slate-950/40">
      <h2 className="text-lg font-semibold text-slate-100">{title}</h2>
      {subtitle ? <p className="mt-1 text-sm text-slate-400">{subtitle}</p> : null}
      <div className="mt-4">{children}</div>
    </section>
  );
}

export default function App() {
  return (
    <main className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-950 to-slate-900 text-slate-100 p-6 md:p-8">
      <div className="mx-auto max-w-6xl space-y-6">
        <header className="rounded-xl border border-slate-800 bg-slate-900 p-6">
          <div className="flex flex-wrap items-center gap-3">
            <h1 className="text-3xl font-bold">Unified User Management System</h1>
            <Badge tone="success">Backend Auth In Progress</Badge>
          </div>
          <p className="mt-3 max-w-3xl text-slate-300">
            This UI now reflects the authentication work that is already implemented on the backend, including
            register/login/refresh token flows and security hardening.
          </p>
        </header>

        <div className="grid gap-6 lg:grid-cols-2">
          <SectionCard title="Completed backend features" subtitle="Delivered and available in the API module">
            <ul className="space-y-2">
              {completedBackendFeatures.map((feature) => (
                <li
                  key={feature}
                  className="flex items-start gap-3 rounded-lg border border-emerald-500/20 bg-emerald-500/5 px-3 py-2 text-sm"
                >
                  <span className="mt-0.5 text-emerald-400">✓</span>
                  <span>{feature}</span>
                </li>
              ))}
            </ul>
          </SectionCard>

          <SectionCard title="Next planned milestones" subtitle="Pending tasks from project worklog">
            <ul className="space-y-2">
              {upcomingFeatures.map((feature) => (
                <li key={feature} className="flex items-start gap-3 rounded-lg border border-slate-700 bg-slate-800/80 px-3 py-2 text-sm">
                  <span className="mt-0.5 text-cyan-400">→</span>
                  <span>{feature}</span>
                </li>
              ))}
            </ul>
          </SectionCard>
        </div>

        <SectionCard title="Auth API status board" subtitle="Endpoints currently exposed by the backend">
          <div className="overflow-x-auto">
            <table className="min-w-full border-separate border-spacing-y-2 text-sm">
              <thead>
                <tr className="text-left text-slate-400">
                  <th className="px-3">Method</th>
                  <th className="px-3">Path</th>
                  <th className="px-3">Status</th>
                  <th className="px-3">Notes</th>
                </tr>
              </thead>
              <tbody>
                {apiEndpoints.map((api) => (
                  <tr key={api.path} className="rounded-lg bg-slate-800/80">
                    <td className="px-3 py-2">
                      <Badge tone="info">{api.method}</Badge>
                    </td>
                    <td className="px-3 py-2 font-mono text-cyan-200">{api.path}</td>
                    <td className="px-3 py-2">
                      <Badge tone="success">{api.status}</Badge>
                    </td>
                    <td className="px-3 py-2 text-slate-300">{api.notes}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </SectionCard>
      </div>
    </main>
  );
}
