import { useEffect, useMemo, useState } from 'react';
import analyticsImg from './assets/analytics.svg';
import adminDashboardImg from './assets/admin-dashboard.svg';
import authShieldImg from './assets/auth-shield.svg';

const API_BASE = 'http://localhost:8080';
const roles = ['guest', 'user', 'admin', 'developer', 'manager', 'support'];

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
    { label: 'Service Onboarding', section: 'services' },
    { label: 'Auth Playground', section: 'login' },
  ],
  manager: [
    { label: 'View Team Stats', section: 'stats' },
    { label: 'Load Users', section: 'admin' },
    { label: 'Read Docs', section: 'docs' },
  ],
  support: [
    { label: 'Account Recovery', section: 'recovery' },
    { label: 'Lookup Users', section: 'admin' },
    { label: 'API Docs', section: 'docs' },
  ],
};

const endpointRows = [
  ['POST', '/api/v1/auth/register', 'Register user'],
  ['POST', '/api/v1/auth/login', 'Primary login'],
  ['POST', '/api/v1/auth/refresh', 'Refresh JWT tokens'],
  ['POST', '/api/v1/auth/mfa/challenge', 'Begin MFA OTP'],
  ['POST', '/api/v1/auth/recovery/challenge', 'Recovery challenge'],
  ['GET', '/api/v1/admin/users', 'List users (admin)'],
  ['POST', '/api/v1/services', 'Onboard tenant service'],
];

const accountStatuses = ['ACTIVE', 'LOCKED', 'DISABLED'];

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
    warn: 'bg-amber-500/15 border-amber-500/30 text-amber-300',
  };

  return <span className={`rounded-full border px-2 py-1 text-xs ${tones[tone]}`}>{children}</span>;
}

async function callApi(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, options);
  const text = await response.text();
  const data = text ? JSON.parse(text) : null;
  if (!response.ok) {
    throw new Error(data?.error || `Request failed (${response.status})`);
  }
  return data;
}


function authHeaders(loginResult) {
  if (!loginResult?.accessToken) {
    return {};
  }
  return { Authorization: `Bearer ${loginResult.accessToken}` };
}

function parseJwtPayload(token) {
  const raw = token?.split('.')?.[1];
  if (!raw) {
    return null;
  }

  const normalized = raw.replace(/-/g, '+').replace(/_/g, '/');
  const padded = normalized + '='.repeat((4 - (normalized.length % 4)) % 4);

  try {
    return JSON.parse(atob(padded));
  } catch (_) {
    return null;
  }
}

function roleFromToken(loginResult) {
  if (!loginResult?.accessToken) {
    return null;
  }

  const payload = parseJwtPayload(loginResult.accessToken);
  const tokenRoles = payload?.roles || [];
  if (Array.isArray(tokenRoles) && tokenRoles.length > 0) {
    return tokenRoles[0].replace('ROLE_', '').toLowerCase();
  }
  return null;
}

export default function App() {
  const [selectedRole, setSelectedRole] = useState('guest');
  const [activeSection, setActiveSection] = useState('home');

  const [registerForm, setRegisterForm] = useState({ username: '', email: '', password: '' });
  const [loginForm, setLoginForm] = useState({ username: '', password: '' });
  const [registerResult, setRegisterResult] = useState(null);
  const [loginResult, setLoginResult] = useState(null);
  const [recoverySetup, setRecoverySetup] = useState({ username: '', securityQuestion: '', securityAnswer: '' });
  const [recoveryChallenge, setRecoveryChallenge] = useState({ username: '', securityAnswer: '' });
  const [recoveryReset, setRecoveryReset] = useState({ challengeId: '', otp: '', newPassword: '' });
  const [recoveryResult, setRecoveryResult] = useState(null);
  const [error, setError] = useState('');

  const [adminUsers, setAdminUsers] = useState([]);
  const [servicesData, setServicesData] = useState([]);
  const [adminEdit, setAdminEdit] = useState({});

  const availableActions = useMemo(() => roleActions[selectedRole] ?? roleActions.guest, [selectedRole]);

  const stats = {
    activeUsers: adminUsers.length || 12480,
    mfaEnabled: Math.max(0, Math.round((adminUsers.length || 12480) * 0.7)),
    onboardedServices: servicesData.length || 214,
  };

  useEffect(() => {
    if (!loginResult?.accessToken) {
      return;
    }

    const tokenRole = roleFromToken(loginResult);
    if (!tokenRole) {
      return;
    }

    if (tokenRole === 'admin') {
      onLoadAdmin();
      onLoadServices();
      setActiveSection('admin');
      return;
    }

    if (tokenRole === 'developer' || tokenRole === 'manager') {
      onLoadServices();
    }
  }, [loginResult]);

  const onRegister = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const data = await callApi('/api/v1/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(registerForm),
      });
      setRegisterResult(data);
      setActiveSection('login');
    } catch (err) {
      setError(err.message);
    }
  };

  const onLogin = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const data = await callApi('/api/v1/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(loginForm),
      });
      setLoginResult(data);
      const tokenRole = roleFromToken(data);
      if (tokenRole) {
        setSelectedRole(tokenRole);
      }
      setActiveSection(tokenRole === 'admin' ? 'admin' : 'stats');
    } catch (err) {
      setError(err.message);
    }
  };

  const onLoadAdmin = async () => {
    setError('');
    try {
      const data = await callApi('/api/v1/admin/users', {
        headers: authHeaders(loginResult),
      });
      setAdminUsers(data);
    } catch (err) {
      setError(err.message);
    }
  };

  const onLoadServices = async () => {
    setError('');
    try {
      const data = await callApi('/api/v1/services', {
        headers: authHeaders(loginResult),
      });
      setServicesData(data);
    } catch (err) {
      setError(err.message);
    }
  };

  const onSetupRecovery = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await callApi('/api/v1/auth/recovery/setup', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(recoverySetup),
      });
      setRecoveryResult('Security question saved.');
    } catch (err) {
      setError(err.message);
    }
  };

  const onChallengeRecovery = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const challenge = await callApi('/api/v1/auth/recovery/challenge', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(recoveryChallenge),
      });
      setRecoveryReset((prev) => ({ ...prev, challengeId: challenge.challengeId, otp: challenge.debugOtp }));
      setRecoveryResult(`Challenge created. OTP(debug): ${challenge.debugOtp}`);
      setActiveSection('recovery');
    } catch (err) {
      setError(err.message);
    }
  };

  const onResetPassword = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await callApi('/api/v1/auth/recovery/reset', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(recoveryReset),
      });
      setRecoveryResult('Password reset successful. You can login with new password.');
    } catch (err) {
      setError(err.message);
    }
  };



  const onAdminEditChange = (userId, field, value) => {
    setAdminEdit((prev) => ({
      ...prev,
      [userId]: {
        ...prev[userId],
        [field]: value,
      },
    }));
  };

  const onAdminSave = async (user) => {
    setError('');
    try {
      const draft = adminEdit[user.id] || {};
      const payload = {
        accountStatus: draft.accountStatus || user.accountStatus,
      };

      if (draft.password && draft.password.trim()) {
        payload.password = draft.password;
      }

      if (draft.roleCodes && draft.roleCodes.trim()) {
        payload.roleCodes = draft.roleCodes
          .split(',')
          .map((code) => code.trim())
          .filter(Boolean);
      }

      await callApi(`/api/v1/admin/users/${user.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          ...authHeaders(loginResult),
        },
        body: JSON.stringify(payload),
      });
      await onLoadAdmin();
      setAdminEdit((prev) => ({
        ...prev,
        [user.id]: {
          accountStatus: payload.accountStatus,
          password: '',
          roleCodes: payload.roleCodes ? payload.roleCodes.join(', ') : '',
        },
      }));
    } catch (err) {
      setError(err.message);
    }
  };

  const onAdminDelete = async (userId) => {
    setError('');
    try {
      await callApi(`/api/v1/admin/users/${userId}`, {
        method: 'DELETE',
        headers: authHeaders(loginResult),
      });
      await onLoadAdmin();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <main className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-950 to-slate-900 text-slate-100">
      <div className="mx-auto max-w-7xl px-6 py-8 space-y-8">
        <header className="rounded-2xl border border-slate-800 bg-slate-900/90 p-7">
          <div className="flex flex-wrap items-center gap-3">
            <h1 className="text-4xl font-bold">Unified User Management System</h1>
            <Badge tone="success">Connected UI + API</Badge>
          </div>
          <p className="mt-3 max-w-4xl text-slate-300">
            Beautiful role-based portal for authentication, administration, user insights, and API integration docs.
            Switch roles to access targeted actions and call backend endpoints directly from the UI.
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
            {loginResult?.accessToken ? <Badge tone="success">Authenticated</Badge> : <Badge tone="warn">Not logged in</Badge>}
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

          {error ? <p className="mt-4 text-sm text-rose-300">{error}</p> : null}
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
            <div id="register" className="space-y-4">
              <form className="space-y-3" onSubmit={onRegister}>
                <p className="text-sm text-slate-400">Register</p>
                <div className="grid gap-3 sm:grid-cols-3">
                  <input className="rounded-lg border border-slate-700 bg-slate-800 px-3 py-2" placeholder="Username" value={registerForm.username} onChange={(e) => setRegisterForm((v) => ({ ...v, username: e.target.value }))} />
                  <input className="rounded-lg border border-slate-700 bg-slate-800 px-3 py-2" placeholder="Email" value={registerForm.email} onChange={(e) => setRegisterForm((v) => ({ ...v, email: e.target.value }))} />
                  <input className="rounded-lg border border-slate-700 bg-slate-800 px-3 py-2" type="password" placeholder="Password" value={registerForm.password} onChange={(e) => setRegisterForm((v) => ({ ...v, password: e.target.value }))} />
                </div>
                <button className="rounded-lg bg-cyan-600 px-4 py-2 text-sm">Create account</button>
              </form>

              <form id="login" className="space-y-3" onSubmit={onLogin}>
                <p className="text-sm text-slate-400">Login</p>
                <div className="grid gap-3 sm:grid-cols-2">
                  <input className="rounded-lg border border-slate-700 bg-slate-800 px-3 py-2" placeholder="Username" value={loginForm.username} onChange={(e) => setLoginForm((v) => ({ ...v, username: e.target.value }))} />
                  <input className="rounded-lg border border-slate-700 bg-slate-800 px-3 py-2" type="password" placeholder="Password" value={loginForm.password} onChange={(e) => setLoginForm((v) => ({ ...v, password: e.target.value }))} />
                </div>
                <div className="flex flex-wrap gap-2">
                  <button className="rounded-lg bg-emerald-600 px-4 py-2 text-sm">Login</button>
                  <button type="button" className="rounded-lg border border-slate-600 px-4 py-2 text-sm" onClick={() => setActiveSection('recovery')}>Forgot Password</button>
                </div>
              </form>

              {registerResult ? <p className="text-xs text-emerald-300">Registered: token issued ({registerResult.tokenType})</p> : null}

              <div id="recovery" className="space-y-3 rounded-lg border border-slate-700 bg-slate-800/40 p-3">
                <p className="text-sm text-slate-300">Forgot password recovery</p>
                <form className="grid gap-2 sm:grid-cols-3" onSubmit={onSetupRecovery}>
                  <input className="rounded-lg border border-slate-700 bg-slate-800 px-3 py-2" placeholder="Username"
                    value={recoverySetup.username} onChange={(e) => setRecoverySetup((v) => ({ ...v, username: e.target.value }))} />
                  <input className="rounded-lg border border-slate-700 bg-slate-800 px-3 py-2" placeholder="Security Question"
                    value={recoverySetup.securityQuestion} onChange={(e) => setRecoverySetup((v) => ({ ...v, securityQuestion: e.target.value }))} />
                  <input className="rounded-lg border border-slate-700 bg-slate-800 px-3 py-2" placeholder="Security Answer"
                    value={recoverySetup.securityAnswer} onChange={(e) => setRecoverySetup((v) => ({ ...v, securityAnswer: e.target.value }))} />
                  <button className="rounded-lg bg-indigo-700 px-3 py-2 text-sm sm:col-span-3">Setup recovery</button>
                </form>

                <form className="grid gap-2 sm:grid-cols-3" onSubmit={onChallengeRecovery}>
                  <input className="rounded-lg border border-slate-700 bg-slate-800 px-3 py-2" placeholder="Username"
                    value={recoveryChallenge.username} onChange={(e) => setRecoveryChallenge((v) => ({ ...v, username: e.target.value }))} />
                  <input className="rounded-lg border border-slate-700 bg-slate-800 px-3 py-2" placeholder="Security Answer"
                    value={recoveryChallenge.securityAnswer} onChange={(e) => setRecoveryChallenge((v) => ({ ...v, securityAnswer: e.target.value }))} />
                  <button className="rounded-lg bg-indigo-600 px-3 py-2 text-sm">Request OTP</button>
                </form>

                <form className="grid gap-2 sm:grid-cols-3" onSubmit={onResetPassword}>
                  <input className="rounded-lg border border-slate-700 bg-slate-800 px-3 py-2" placeholder="Challenge ID"
                    value={recoveryReset.challengeId} onChange={(e) => setRecoveryReset((v) => ({ ...v, challengeId: e.target.value }))} />
                  <input className="rounded-lg border border-slate-700 bg-slate-800 px-3 py-2" placeholder="OTP"
                    value={recoveryReset.otp} onChange={(e) => setRecoveryReset((v) => ({ ...v, otp: e.target.value }))} />
                  <input className="rounded-lg border border-slate-700 bg-slate-800 px-3 py-2" type="password" placeholder="New Password"
                    value={recoveryReset.newPassword} onChange={(e) => setRecoveryReset((v) => ({ ...v, newPassword: e.target.value }))} />
                  <button className="rounded-lg bg-emerald-700 px-3 py-2 text-sm sm:col-span-3">Reset password</button>
                </form>
                {recoveryResult ? <p className="text-xs text-emerald-300">{recoveryResult}</p> : null}
              </div>
            </div>
          </Card>

          <Card title="Admin Panel">
            <div id="admin" className="space-y-3">
              <p className="text-sm text-slate-400">Manage users and roles</p>
              <div className="flex gap-2">
                <button onClick={onLoadAdmin} className="rounded-lg bg-cyan-700 px-4 py-2 text-sm">Load users</button>
                <button onClick={onLoadServices} className="rounded-lg border border-slate-600 px-4 py-2 text-sm">Load services</button>
              </div>

              {!loginResult?.accessToken ? (
                <p className="rounded-lg border border-amber-600/30 bg-amber-500/10 p-3 text-xs text-amber-200">
                  Please login with an admin account to use user management actions.
                </p>
              ) : null}

              <div className="max-h-96 overflow-auto rounded-lg border border-slate-700 bg-slate-800/70 p-2 text-xs">
                {adminUsers.length === 0 ? (
                  'No users loaded yet.'
                ) : (
                  <div className="space-y-2">
                    {adminUsers.map((u) => {
                      const draft = adminEdit[u.id] || {
                        accountStatus: u.accountStatus,
                        password: '',
                        roleCodes: (u.roles || []).join(', '),
                      };

                      return (
                        <div key={u.id} className="rounded-lg border border-slate-700 bg-slate-900/60 p-3 space-y-2">
                          <div className="flex flex-wrap items-center justify-between gap-2">
                            <p className="text-sm text-slate-200">#{u.id} {u.username} ({u.email})</p>
                            <p className="text-[11px] text-slate-400">Failed logins: {u.failedLoginAttempts}</p>
                          </div>
                          <div className="grid gap-2 sm:grid-cols-3">
                            <select
                              className="rounded-lg border border-slate-700 bg-slate-800 px-2 py-2"
                              value={draft.accountStatus}
                              onChange={(e) => onAdminEditChange(u.id, 'accountStatus', e.target.value)}
                            >
                              {accountStatuses.map((status) => (
                                <option key={status} value={status}>{status}</option>
                              ))}
                            </select>
                            <input
                              className="rounded-lg border border-slate-700 bg-slate-800 px-2 py-2"
                              placeholder="Roles (comma-separated)"
                              value={draft.roleCodes}
                              onChange={(e) => onAdminEditChange(u.id, 'roleCodes', e.target.value)}
                            />
                            <input
                              className="rounded-lg border border-slate-700 bg-slate-800 px-2 py-2"
                              type="password"
                              placeholder="New password (optional)"
                              value={draft.password}
                              onChange={(e) => onAdminEditChange(u.id, 'password', e.target.value)}
                            />
                          </div>
                          <div className="flex gap-2">
                            <button type="button" onClick={() => onAdminSave(u)} className="rounded-lg bg-emerald-700 px-3 py-1.5">Save</button>
                            <button type="button" onClick={() => onAdminDelete(u.id)} className="rounded-lg border border-rose-500/70 px-3 py-1.5 text-rose-200">Delete</button>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>
            </div>
          </Card>

          <Card title="User Stats">
            <div id="stats" className="grid gap-3 sm:grid-cols-3 text-sm">
              <div className="rounded-lg border border-slate-700 bg-slate-800/80 p-3">
                <p className="text-slate-400">Active users</p>
                <p className="mt-1 text-2xl font-semibold text-emerald-300">{stats.activeUsers}</p>
              </div>
              <div className="rounded-lg border border-slate-700 bg-slate-800/80 p-3">
                <p className="text-slate-400">MFA enabled (est.)</p>
                <p className="mt-1 text-2xl font-semibold text-cyan-300">{stats.mfaEnabled}</p>
              </div>
              <div className="rounded-lg border border-slate-700 bg-slate-800/80 p-3">
                <p className="text-slate-400">Onboarded services</p>
                <p className="mt-1 text-2xl font-semibold text-violet-300">{stats.onboardedServices}</p>
              </div>
            </div>
          </Card>

          <Card title="Documentation & Onboarding">
            <div id="docs" className="space-y-2 text-sm">
              <p>Swagger UI: <a className="text-cyan-300 underline" href={`${API_BASE}/swagger-ui.html`} target="_blank" rel="noreferrer">/swagger-ui.html</a></p>
              <p>OpenAPI JSON: <a className="text-cyan-300 underline" href={`${API_BASE}/v3/api-docs`} target="_blank" rel="noreferrer">/v3/api-docs</a></p>
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
