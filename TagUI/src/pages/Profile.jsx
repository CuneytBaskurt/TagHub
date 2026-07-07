import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { User, Mail, Hash, ArrowLeft } from 'lucide-react';
function parseJwt(token) {
  try {
    if (!token) return null;
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function(c) {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
    return JSON.parse(jsonPayload);
  } catch (e) {
    return null;
  }
}

export default function Profile() {
  const [profileData, setProfileData] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      navigate('/login');
      return;
    }

    const decoded = parseJwt(token);
    const email = localStorage.getItem('userEmail') || decoded?.sub || 'Bilinmiyor';
    const username = email !== 'Bilinmiyor' ? email.split('@')[0] : 'Kullanıcı';
    
    setProfileData({
      email: email,
      username: username,
      userId: decoded?.userId || 'N/A',
    });
  }, [navigate]);

  if (!profileData) {
    return <div className="auth-layout"><p>Yükleniyor...</p></div>;
  }

  return (
    <div className="auth-layout">
      <div className="glass-container animate-fade-in" style={{ maxWidth: '600px' }}>
        <div style={{ display: 'flex', alignItems: 'center', marginBottom: '24px', gap: '12px' }}>
          <User size={32} color="var(--primary-color)" />
          <h2 style={{ margin: 0 }}>My Profile</h2>
        </div>
        
        <p className="subtitle" style={{ marginBottom: '32px' }}>
          Buradaki bilgiler güvenli bir şekilde JWT token içerisinden ve yerel depolamadan alınmıştır. 
          (Backend profil ucu henüz hazır olmadığı için mevcut veriler derlenmiştir.)
        </p>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          
          <div style={{ background: 'rgba(0,0,0,0.2)', padding: '16px', borderRadius: '8px', border: '1px solid var(--border-color)', display: 'flex', alignItems: 'center', gap: '16px' }}>
            <div style={{ background: 'rgba(255,255,255,0.05)', padding: '12px', borderRadius: '50%' }}>
              <User size={24} color="var(--text-primary)" />
            </div>
            <div>
              <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '4px' }}>Username</div>
              <div style={{ fontSize: '1.1rem', fontWeight: '500' }}>{profileData.username}</div>
            </div>
          </div>

          
          <div style={{ background: 'rgba(0,0,0,0.2)', padding: '16px', borderRadius: '8px', border: '1px solid var(--border-color)', display: 'flex', alignItems: 'center', gap: '16px' }}>
            <div style={{ background: 'rgba(255,255,255,0.05)', padding: '12px', borderRadius: '50%' }}>
              <Mail size={24} color="var(--text-primary)" />
            </div>
            <div>
              <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '4px' }}>Email Address</div>
              <div style={{ fontSize: '1.1rem', fontWeight: '500' }}>{profileData.email}</div>
            </div>
          </div>

          
          <div style={{ background: 'rgba(0,0,0,0.2)', padding: '16px', borderRadius: '8px', border: '1px solid var(--border-color)', display: 'flex', alignItems: 'center', gap: '16px' }}>
            <div style={{ background: 'rgba(255,255,255,0.05)', padding: '12px', borderRadius: '50%' }}>
              <Hash size={24} color="var(--text-primary)" />
            </div>
            <div>
              <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '4px' }}>User ID</div>
              <div style={{ fontSize: '1.1rem', fontWeight: '500' }}>{profileData.userId}</div>
            </div>
          </div>
        </div>

        <div style={{ marginTop: '32px' }}>
          <Link to="/dashboard" className="btn-primary" style={{ textDecoration: 'none', display: 'inline-flex', width: 'auto', padding: '12px 24px' }}>
            <ArrowLeft size={18} />
            Back to Dashboard
          </Link>
        </div>
      </div>
    </div>
  );
}

