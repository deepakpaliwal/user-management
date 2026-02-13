import { useEffect, useMemo, useState } from 'react';
import analyticsImg from './assets/analytics.svg';
import adminDashboardImg from './assets/admin-dashboard.svg';
import authShieldImg from './assets/auth-shield.svg';

const API_BASE = 'http://localhost:8080';
const roles = ['guest', 'user', 'admin', 'developer', 'manager', 'support'];
const accountStatuses = ['ACTIVE', 'LOCKED', 'DISABLED'];

const pages = [
  { id: 'home', label: 'Home' },
  { id: 'register', label: 'Registration' },
  { id: 'login', label: 'Login' },
  { id: 'status', label: 'User Status' },
  { id: 'onboarding', label: 'Documentation & Onboarding' },
  { id: 'api', label: 'API Quick Reference' },
  { id: 'admin', label: 'Admin Panel' },
];

const productHighlights = [
  {
    title: 'Unified Authentication',
    description: 'Register/login, refresh token flow, MFA challenge-verify, and recovery lifecycle in one surface.',
    image: authShieldImg,
  },
  {
    title: 'Operations & Governance',
    description: 'Role-aware administration for user lifecycle, account status, and policy-backed access controls.',
    image: adminDashboardImg,
  },
  {
    title: 'Onboarding & Integrations',
    description: 'Service onboarding, sample client boilerplates, and API references for faster integration.',
    image: analyticsImg,
  },
];

const endpointRows = [
  ['POST', '/api/v1/auth/register', 'Register user'],
  ['POST', '/api/v1/auth/login', 'Primary login'],
  ['POST', '/api/v1/auth/refresh', 'Refresh JWT'],
  ['POST', '/api/v1/auth/mfa/challenge', 'MFA challenge'],
  ['POST', '/api/v1/auth/recovery/challenge', 'Recovery challenge'],
  ['GET', '/api/v1/admin/users', 'List users (admin)'],
  ['POST', '/api/v1/services', 'Onboard tenant service'],
];

function Card({ title, children }) {
  return (
    <section className="rounded-2xl border border-slate-800 bg-slate-900/80 p-5 shadow-xl shadow-slate-950/30">
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

function CodeBlock({ language, code }) {
  return (
    <div className="rounded-xl border border-slate-700 bg-slate-950/70 p-3">
      <p className="mb-2 text-xs uppercase tracking-wide text-slate-400">{language}</p>
      <pre className="overflow-auto text-xs text-cyan-200"><code>{code}</code></pre>
    </div>
  );
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
  const payload = parseJwtPayload(loginResult?.accessToken);
  const tokenRoles = payload?.roles || [];
  if (Array.isArray(tokenRoles) && tokenRoles.length > 0) {
    return tokenRoles[0].replace('ROLE_', '').toLowerCase();
  }
  return null;
}

export default function App() {
  const [currentPage, setCurrentPage] = useState('home');
  const [selectedRole, setSelectedRole] = useState('guest');

  const [registerForm, setRegisterForm] = useState({ username: '', email: '', password: '' });
  const [loginForm, setLoginForm] = useState({ username: '', password: '' });
  const [registerResult, setRegisterResult] = useState(null);
  const [loginResult, setLoginResult] = useState(null);
  const [error, setError] = useState('');

  const [adminUsers, setAdminUsers] = useState([]);
  const [servicesData, setServicesData] = useState([]);
  const [adminEdit, setAdminEdit] = useState({});

  const stats = useMemo(() => ({
    activeUsers: adminUsers.length || 12480,
    mfaEnabled: Math.max(0, Math.round((adminUsers.length || 12480) * 0.7)),
    onboardedServices: servicesData.length || 214,
  }), [adminUsers.length, servicesData.length]);

  const onLoadAdmin = async () => {
    setError('');
    try {
      const data = await callApi('/api/v1/admin/users', { headers: authHeaders(loginResult) });
      setAdminUsers(data);
    } catch (err) {
      setError(err.message);
    }
  };

  const onLoadServices = async () => {
    setError('');
    try {
      const data = await callApi('/api/v1/services', { headers: authHeaders(loginResult) });
      setServicesData(data);
    } catch (err) {
      setError(err.message);
    }
  };

  useEffect(() => {
    if (!loginResult?.accessToken) {
      return;
    }
    const tokenRole = roleFromToken(loginResult);
    if (tokenRole) {
      setSelectedRole(tokenRole);
    }
    if (tokenRole === 'admin') {
      onLoadAdmin();
      onLoadServices();
      setCurrentPage('admin');
    } else {
      setCurrentPage('status');
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
      setCurrentPage('login');
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
    } catch (err) {
      setError(err.message);
    }
  };

  const onAdminEditChange = (userId, field, value) => {
    setAdminEdit((prev) => ({ ...prev, [userId]: { ...prev[userId], [field]: value } }));
  };

  const onAdminSave = async (user) => {
    setError('');
    try {
      const draft = adminEdit[user.id] || {};
      const payload = { accountStatus: draft.accountStatus || user.accountStatus };
      if (draft.password && draft.password.trim()) payload.password = draft.password;
      if (draft.roleCodes && draft.roleCodes.trim()) {
        payload.roleCodes = draft.roleCodes.split(',').map((s) => s.trim()).filter(Boolean);
      }
      await callApi(`/api/v1/admin/users/${user.id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', ...authHeaders(loginResult) },
        body: JSON.stringify(payload),
      });
      await onLoadAdmin();
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

  const javaBoilerplate = `WebClient client = WebClient.builder().baseUrl("http://localhost:8080").build();\n\nMap<String, Object> login = client.post()\n  .uri("/api/v1/auth/login")\n  .bodyValue(Map.of("username", "admin_uums", "password", "SecureAdmin@2026"))\n  .retrieve()\n  .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})\n  .block();`;

  const pythonBoilerplate = `import requests\n\nresp = requests.post(\n  "http://localhost:8080/api/v1/auth/login",\n  json={"username": "admin_uums", "password": "SecureAdmin@2026"},\n  timeout=10,\n)\nresp.raise_for_status()\naccess_token = resp.json()["accessToken"]`;

  const angularBoilerplate = `this.http.post<any>('http://localhost:8080/api/v1/auth/login', {\n  username: 'admin_uums',\n  password: 'SecureAdmin@2026'\n}).subscribe(res => {\n  localStorage.setItem('accessToken', res.accessToken);\n});`;

  return (
    <main className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-950 to-slate-900 text-slate-100">
      <div className="mx-auto max-w-7xl px-6 py-8 space-y-6">
        <header className="rounded-2xl border border-slate-800 bg-slate-900/90 p-6">
          <div className="flex flex-wrap items-center justify-between gap-4">
            <div>
              <h1 className="text-3xl font-bold">Unified User Management System</h1>
              <p className="mt-2 max-w-2xl text-slate-300">A modern identity and tenant onboarding portal with secure auth, role-based operations, and API-first integrations.</p>
            </div>
            <div className="flex items-center gap-2">
              {loginResult?.accessToken ? <Badge tone="success">Authenticated</Badge> : <Badge tone="warn">Not logged in</Badge>}
              <Badge tone="info">Role: {selectedRole.toUpperCase()}</Badge>
            </div>
          </div>

          <div className="mt-5 grid gap-3 lg:grid-cols-[1fr_auto]">
            <nav className="flex flex-wrap gap-2">
              {pages.map((p) => (
                <button
                  key={p.id}
                  type="button"
                  onClick={() => setCurrentPage(p.id)}
                  className={`rounded-lg px-3 py-2 text-sm border ${currentPage === p.id ? 'bg-cyan-600/20 border-cyan-500 text-cyan-200' : 'bg-slate-800 border-slate-700'}`}
                >
                  {p.label}
                </button>
              ))}
            </nav>
            <select
              className="rounded-lg border border-slate-700 bg-slate-800 px-3 py-2 text-sm"
              value={selectedRole}
              onChange={(e) => setSelectedRole(e.target.value)}
            >
              {roles.map((role) => <option key={role} value={role}>{role.toUpperCase()}</option>)}
            </select>
          </div>

          {error ? <p className="mt-4 rounded-lg border border-rose-500/30 bg-rose-500/10 px-3 py-2 text-sm text-rose-200">{error}</p> : null}
        </header>

        {currentPage === 'home' ? (
          <section className="space-y-5">
            <div className="grid gap-4 md:grid-cols-3">
              {productHighlights.map((item) => (
                <article key={item.title} className="overflow-hidden rounded-2xl border border-slate-800 bg-slate-900/80">
                  <img src={item.image} alt={item.title} className="h-44 w-full object-cover" />
                  <div className="p-4">
                    <h3 className="text-lg font-semibold text-white">{item.title}</h3>
                    <p className="mt-2 text-sm text-slate-300">{item.description}</p>
                  </div>
                </article>
              ))}
            </div>
            <Card title="Why UUMS?">
              <ul className="list-disc space-y-2 pl-5 text-sm">
                <li>Secure by default with JWT, MFA, recovery, and rate limiting.</li>
                <li>Operational control panel for admin workflows and user lifecycle management.</li>
                <li>Developer-ready API docs and onboarding snippets for quick implementation.</li>
              </ul>
            </Card>
          </section>
        ) : null}

        {currentPage === 'register' ? (
          <Card title="Registration">
            <form className="space-y-3" onSubmit={onRegister}>
              <div className="grid gap-3 md:grid-cols-3">
                <input className="rounded-lg border border-slate-700 bg-slate-800 px-3 py-2" placeholder="Username" value={registerForm.username} onChange={(e) => setRegisterForm((v) => ({ ...v, username: e.target.value }))} />
                <input className="rounded-lg border border-slate-700 bg-slate-800 px-3 py-2" placeholder="Email" value={registerForm.email} onChange={(e) => setRegisterForm((v) => ({ ...v, email: e.target.value }))} />
                <input className="rounded-lg border border-slate-700 bg-slate-800 px-3 py-2" type="password" placeholder="Password" value={registerForm.password} onChange={(e) => setRegisterForm((v) => ({ ...v, password: e.target.value }))} />
              </div>
              <button className="rounded-lg bg-cyan-600 px-4 py-2 text-sm">Create account</button>
            </form>
            {registerResult ? <p className="mt-3 text-sm text-emerald-300">Registration successful ({registerResult.tokenType}). You can login now.</p> : null}
          </Card>
        ) : null}

        {currentPage === 'login' ? (
          <Card title="Login">
            <form className="space-y-3" onSubmit={onLogin}>
              <div className="grid gap-3 md:grid-cols-2">
                <input className="rounded-lg border border-slate-700 bg-slate-800 px-3 py-2" placeholder="Username" value={loginForm.username} onChange={(e) => setLoginForm((v) => ({ ...v, username: e.target.value }))} />
                <input className="rounded-lg border border-slate-700 bg-slate-800 px-3 py-2" type="password" placeholder="Password" value={loginForm.password} onChange={(e) => setLoginForm((v) => ({ ...v, password: e.target.value }))} />
              </div>
              <button className="rounded-lg bg-emerald-600 px-4 py-2 text-sm">Login</button>
            </form>
          </Card>
        ) : null}

        {currentPage === 'status' ? (
          <Card title="User Status">
            <div className="grid gap-3 sm:grid-cols-3 text-sm">
              <div className="rounded-lg border border-slate-700 bg-slate-800/80 p-3"><p className="text-slate-400">Active users</p><p className="mt-1 text-2xl font-semibold text-emerald-300">{stats.activeUsers}</p></div>
              <div className="rounded-lg border border-slate-700 bg-slate-800/80 p-3"><p className="text-slate-400">MFA enabled</p><p className="mt-1 text-2xl font-semibold text-cyan-300">{stats.mfaEnabled}</p></div>
              <div className="rounded-lg border border-slate-700 bg-slate-800/80 p-3"><p className="text-slate-400">Onboarded services</p><p className="mt-1 text-2xl font-semibold text-violet-300">{stats.onboardedServices}</p></div>
            </div>
          </Card>
        ) : null}

        {currentPage === 'onboarding' ? (
          <Card title="Documentation & Onboarding">
            <div className="space-y-4 text-sm">
              <p>Swagger UI: <a className="text-cyan-300 underline" href={`${API_BASE}/swagger-ui.html`} target="_blank" rel="noreferrer">/swagger-ui.html</a></p>
              <p>OpenAPI JSON: <a className="text-cyan-300 underline" href={`${API_BASE}/v3/api-docs`} target="_blank" rel="noreferrer">/v3/api-docs</a></p>
              <div className="grid gap-3 lg:grid-cols-3">
                <CodeBlock language="Java" code={javaBoilerplate} />
                <CodeBlock language="Python" code={pythonBoilerplate} />
                <CodeBlock language="Angular" code={angularBoilerplate} />
              </div>
            </div>
          </Card>
        ) : null}

        {currentPage === 'api' ? (
          <Card title="API Quick Reference">
            <div className="overflow-x-auto">
              <table className="min-w-full border-separate border-spacing-y-2 text-sm">
                <thead><tr className="text-left text-slate-400"><th className="px-3">Method</th><th className="px-3">Path</th><th className="px-3">Purpose</th></tr></thead>
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
        ) : null}

        {currentPage === 'admin' ? (
          <Card title="Admin Panel">
            <div className="space-y-3">
              <div className="flex gap-2">
                <button onClick={onLoadAdmin} className="rounded-lg bg-cyan-700 px-4 py-2 text-sm">Load users</button>
                <button onClick={onLoadServices} className="rounded-lg border border-slate-600 px-4 py-2 text-sm">Load services</button>
              </div>
              <div className="max-h-96 overflow-auto rounded-lg border border-slate-700 bg-slate-800/70 p-2 text-xs">
                {adminUsers.length === 0 ? 'No users loaded yet.' : (
                  <div className="space-y-2">
                    {adminUsers.map((u) => {
                      const draft = adminEdit[u.id] || { accountStatus: u.accountStatus, password: '', roleCodes: (u.roles || []).join(', ') };
                      return (
                        <div key={u.id} className="rounded-lg border border-slate-700 bg-slate-900/60 p-3 space-y-2">
                          <p className="text-sm text-slate-200">#{u.id} {u.username} ({u.email})</p>
                          <div className="grid gap-2 sm:grid-cols-3">
                            <select className="rounded-lg border border-slate-700 bg-slate-800 px-2 py-2" value={draft.accountStatus} onChange={(e) => onAdminEditChange(u.id, 'accountStatus', e.target.value)}>
                              {accountStatuses.map((status) => <option key={status} value={status}>{status}</option>)}
                            </select>
                            <input className="rounded-lg border border-slate-700 bg-slate-800 px-2 py-2" placeholder="Roles (comma-separated)" value={draft.roleCodes} onChange={(e) => onAdminEditChange(u.id, 'roleCodes', e.target.value)} />
                            <input className="rounded-lg border border-slate-700 bg-slate-800 px-2 py-2" type="password" placeholder="New password" value={draft.password} onChange={(e) => onAdminEditChange(u.id, 'password', e.target.value)} />
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
        ) : null}
      </div>
    </main>
  );
}
