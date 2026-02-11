const milestones = [
  'SSO + OAuth2 foundation',
  'MFA and account recovery',
  'Tenant onboarding and API keys',
  'RBAC management dashboard',
  'Traffic analytics and alerts',
];

export default function App() {
  return (
    <main className="min-h-screen bg-slate-950 text-slate-100 p-8">
      <section className="mx-auto max-w-4xl rounded-lg border border-slate-800 bg-slate-900 p-8 shadow-xl">
        <h1 className="text-3xl font-bold">Unified User Management System</h1>
        <p className="mt-2 text-slate-300">
          Project scaffold initialized. This dashboard will evolve into an admin and onboarding experience.
        </p>

        <h2 className="mt-6 text-xl font-semibold">Initial build milestones</h2>
        <ul className="mt-3 space-y-2">
          {milestones.map((milestone) => (
            <li key={milestone} className="rounded-md border border-slate-700 bg-slate-800 px-3 py-2">
              {milestone}
            </li>
          ))}
        </ul>
      </section>
    </main>
  );
}
