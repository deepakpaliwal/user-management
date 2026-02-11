import { useMemo, useState } from 'react';
import analyticsImg from './assets/analytics.svg';
import adminDashboardImg from './assets/admin-dashboard.svg';
import authShieldImg from './assets/auth-shield.svg';

const roles = ['guest', 'user', 'admin', 'developer'];

const services = [
  {
    title: 'Unified Authentication',
    description: 'JWT + refresh strategy, MFA challenge/verify, and recovery via security questions + OTP.',
    image: authShieldImg,
  },
  {
    title: 'Admin Operations',
    description: 'User CRUD, account state management, role assignment, and policy-enforced controls.',
    image: adminDashboardImg,
  },
  {
    title: 'Tenant & Analytics Foundation',
    description: 'Service onboarding with API keys, tier limits, and traffic insights roadmap.',
    image: analyticsImg,
  },
];

const roleActions = {
  guest: [
    { label: 'Register Account', section: 'register' },
    { label: 'Login', section: 'login' },
    { label: 'View API Documentation', section: 'docs' },
  ],
  user: [
    { label: 'User Stats', section: 'stats' },
    { label: 'Security Settings', section: 'login' },
    { label: 'API Documentation', section: 'docs' },
  ],
  admin: [
    { label: 'Open Admin Panel', section: 'admin' },
    { label: 'View User Stats', section: 'stats' },
    { label: 'Review API Docs', section: 'docs' },
  ],
  developer: [
    { label: 'Swagger UI', section: 'docs' },
    { label: 'Service Onboarding', section: 'admin' },
    { label: 'Auth Playground', section: 'login' },
  ],
};

const endpointRows = [
  ['POST', '/api/v1/auth/register', 'Register user'],
  ['POST', '/api/v1/auth/login', 'Primary login'],
  ['POST', '/api/v1/auth/refresh', 'Refresh JWT tokens'],
  ['POST', '/api/v1/auth/mfa/challenge', 'Begin MFA OTP'],
  ['POST', '/api/v1/auth/mfa/verify', 'Verify MFA OTP'],
  ['POST', '/api/v1/auth/recovery/challenge', 'Recovery challenge'],
  ['POST', '/api/v1/auth/recovery/reset', 'Password reset'],
  ['GET', '/api/v1/admin/users', 'List users (admin)'],
  ['PUT', '/api/v1/admin/users/{id}', 'Update user state/roles'],
  ['POST', '/api/v1/services', 'Onboard tenant service'],
];

function Card({ title, children }) {
  return (
    <section className="rounded-2xl border border-slate-800 bg-slate-900/80 p-5 shadow-xl shadow-slate-950/40">
      <h2 className="text-xl font-semibold text-white">{title}</h2>
      <div className="mt-4 text-slate-300">{children}</div>
    </section>
  );
}

function Badge({ children, tone = 'default' }) {
  const tones = {
    default: 'bg-slate-800 border-slate-700 text-slate-200',
    success: 'bg-emerald-500/15 border-emerald-500/30 text-emerald-300',
    info: 'bg-cyan-500/15 border-cyan-500/30 text-cyan-300',
  };

  return <span className={`rounded-full border px-2 py-1 text-xs ${tones[tone]}`}>{children}</span>;
}

export default function App() {
  const [selectedRole, setSelectedRole] = useState('guest');
  const [activeSection, setActiveSection] = useState('home');

  const availableActions = useMemo(() => roleActions[selectedRole] ?? roleActions.guest, [selectedRole]);

  return (
    <main className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-950 to-slate-900 text-slate-100">
      <div className="mx-auto max-w-7xl px-6 py-8 space-y-8">
        <header className="rounded-2xl border border-slate-800 bg-slate-900/90 p-7">
          <div className="flex flex-wrap items-center gap-3">
            <h1 className="text-4xl font-bold">Unified User Management System</h1>
            <Badge tone="success">Production-ready foundation</Badge>
          </div>
          <p className="mt-3 max-w-4xl text-slate-300">
            Centralized identity, secure access controls, tenant onboarding, admin operations, and API documentation in
            one hub. Choose a role to preview role-driven actions and navigate available features.
          </p>

          <div className="mt-5 flex flex-wrap items-center gap-3">
            <label htmlFor="role" className="text-sm text-slate-400">Simulated role:</label>
            <select
              id="role"
              className="rounded-lg border border-slate-700 bg-slate-800 px-3 py-2 text-sm"
              value={selectedRole}
              onChange={(e) => setSelectedRole(e.target.value)}
            >
              {roles.map((role) => (
                <option key={role} value={role}>{role.toUpperCase()}</option>
              ))}
            </select>
            <Badge tone="info">Active role: {selectedRole.toUpperCase()}</Badge>
          </div>

          <div className="mt-5 flex flex-wrap gap-3">
            {availableActions.map((action) => (
              <button
                key={action.label}
                type="button"
                onClick={() => setActiveSection(action.section)}
                className="rounded-lg border border-cyan-500/40 bg-cyan-500/10 px-4 py-2 text-sm hover:bg-cyan-500/20"
              >
                {action.label}
              </button>
            ))}
          </div>
        </header>

        <section className="grid gap-6 lg:grid-cols-3">
          {services.map((service) => (
            <article key={service.title} className="overflow-hidden rounded-2xl border border-slate-800 bg-slate-900/80">
              <img src={service.image} alt={service.title} className="h-44 w-full object-cover" />
              <div className="p-4">
                <h2 className="text-lg font-semibold text-white">{service.title}</h2>
                <p className="mt-2 text-sm text-slate-300">{service.description}</p>
              </div>
            </article>
          ))}
        </section>

        <section className="grid gap-6 lg:grid-cols-2">
          <Card title="Authentication">
            <div id="login" className="space-y-3">
              <p className="text-sm text-slate-400">Login and Register form previews</p>
              <div className="grid gap-3 sm:grid-cols-2">
                <input className="rounded-lg border border-slate-700 bg-slate-800 px-3 py-2" placeholder="Username" />
                <input className="rounded-lg border border-slate-700 bg-slate-800 px-3 py-2" type="password" placeholder="Password" />
              </div>
              <div className="flex flex-wrap gap-2">
                <button className="rounded-lg bg-cyan-600 px-4 py-2 text-sm">Login</button>
                <button className="rounded-lg border border-slate-600 px-4 py-2 text-sm" id="register">Register</button>
                <button className="rounded-lg border border-slate-600 px-4 py-2 text-sm">Forgot Password</button>
              </div>
            </div>
          </Card>

          <Card title="Admin Panel">
            <div id="admin" className="space-y-3">
              <p className="text-sm text-slate-400">Role-driven quick operations</p>
              <ul className="space-y-2 text-sm">
                <li className="rounded-lg border border-slate-700 bg-slate-800/80 px-3 py-2">User CRUD + lock/unlock</li>
                <li className="rounded-lg border border-slate-700 bg-slate-800/80 px-3 py-2">Password override + failed attempts reset</li>
                <li className="rounded-lg border border-slate-700 bg-slate-800/80 px-3 py-2">Role assignment and status updates</li>
              </ul>
            </div>
          </Card>

          <Card title="User Stats">
            <div id="stats" className="grid gap-3 sm:grid-cols-3 text-sm">
              <div className="rounded-lg border border-slate-700 bg-slate-800/80 p-3">
                <p className="text-slate-400">Active users</p>
                <p className="mt-1 text-2xl font-semibold text-emerald-300">12,480</p>
              </div>
              <div className="rounded-lg border border-slate-700 bg-slate-800/80 p-3">
                <p className="text-slate-400">MFA enabled</p>
                <p className="mt-1 text-2xl font-semibold text-cyan-300">8,940</p>
              </div>
              <div className="rounded-lg border border-slate-700 bg-slate-800/80 p-3">
                <p className="text-slate-400">Onboarded services</p>
                <p className="mt-1 text-2xl font-semibold text-violet-300">214</p>
              </div>
            </div>
          </Card>

          <Card title="Documentation & Developer Onboarding">
            <div id="docs" className="space-y-3 text-sm">
              <p>Swagger UI: <a className="text-cyan-300 underline" href="http://localhost:8080/swagger-ui.html">/swagger-ui.html</a></p>
              <p>OpenAPI JSON: <a className="text-cyan-300 underline" href="http://localhost:8080/v3/api-docs">/v3/api-docs</a></p>
              <p className="text-slate-400">Action context: {activeSection.toUpperCase()}</p>
            </div>
          </Card>
        </section>

        <Card title="API quick reference">
          <div className="overflow-x-auto">
            <table className="min-w-full border-separate border-spacing-y-2 text-sm">
              <thead>
                <tr className="text-left text-slate-400">
                  <th className="px-3">Method</th>
                  <th className="px-3">Path</th>
                  <th className="px-3">Purpose</th>
                </tr>
              </thead>
              <tbody>
                {endpointRows.map(([method, path, purpose]) => (
                  <tr key={path} className="bg-slate-800/80">
                    <td className="px-3 py-2"><Badge tone="info">{method}</Badge></td>
                    <td className="px-3 py-2 font-mono text-cyan-200">{path}</td>
                    <td className="px-3 py-2 text-slate-300">{purpose}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>
      </div>
    </main>
  );
}
