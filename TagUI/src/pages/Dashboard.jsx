import React, { useState } from 'react';
import { User, LayoutDashboard, KeyRound, LogOut } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
export default function Dashboard() {
  const [roomToken, setRoomToken] = useState('');
  const navigate = useNavigate();

  // Şimdilik token'dan veya localStorage'dan email'i alıp username gibi gösteriyoruz
  const email = localStorage.getItem('userEmail') || 'User';
  const username = email !== 'User' ? email.split('@')[0] : 'User';

  const handleJoinRoom = async (e) => {
    e.preventDefault();
    if (!roomToken.trim()) return;

    try {
      await api.post(`/room/join/${roomToken.trim()}`);
      alert("Odaya başarıyla katıldınız!");
      navigate('/my-rooms');
    } catch (err) {
      console.error('Join room error', err);
      const errorMessage = typeof err.response?.data === 'string' 
        ? err.response.data 
        : (err.response?.data?.message || err.response?.data?.error || err.message || 'Odaya katılırken bir hata oluştu.');
      alert('Hata: ' + errorMessage);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('userEmail');
    navigate('/login');
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      {/* Navbar */}
      <nav style={{ 
        padding: '20px 40px', 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center',
        borderBottom: '1px solid var(--glass-border)',
        background: 'rgba(26, 28, 35, 0.4)',
        backdropFilter: 'blur(10px)'
      }}>
        <div style={{ fontSize: '1.25rem', fontWeight: '600' }}>
          Welcome, <span style={{ color: 'var(--primary-color)' }}>{username}</span>
        </div>
        <div style={{ display: 'flex', gap: '16px' }}>
          <button onClick={() => navigate('/my-rooms')} className="btn-primary" style={{ padding: '8px 16px', background: 'rgba(255,255,255,0.1)', color: 'var(--text-primary)', width: 'auto' }}>
            <LayoutDashboard size={18} />
            My Rooms
          </button>
          <button onClick={() => navigate('/profile')} className="btn-primary" style={{ padding: '8px 16px', background: 'rgba(255,255,255,0.1)', color: 'var(--text-primary)', width: 'auto' }}>
            <User size={18} />
            My Profile
          </button>
          <button onClick={handleLogout} className="btn-primary" style={{ padding: '8px 16px', background: 'rgba(239, 68, 68, 0.2)', color: 'var(--danger-color)', width: 'auto' }}>
            <LogOut size={18} />
            Logout
          </button>
        </div>
      </nav>

      {/* Main Content */}
      <main style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '20px' }}>
        <div className="glass-container animate-fade-in" style={{ textAlign: 'center', maxWidth: '500px' }}>
          <h2 style={{ marginBottom: '8px' }}>Join a Labeling Room</h2>
          <p className="subtitle">Enter the room token provided by your admin to start labeling data.</p>
          
          <form onSubmit={handleJoinRoom}>
            <div className="form-group">
              <div style={{ position: 'relative' }}>
                <KeyRound style={{ position: 'absolute', top: '12px', left: '12px', color: 'var(--text-secondary)' }} size={20} />
                <input 
                  type="text" 
                  className="form-input" 
                  placeholder="Enter Room Token (e.g. X7K-9P2)"
                  value={roomToken}
                  onChange={(e) => setRoomToken(e.target.value)}
                  style={{ width: '100%', paddingLeft: '40px', textAlign: 'center', letterSpacing: '2px', fontSize: '1.1rem' }}
                  required 
                />
              </div>
            </div>

            <button type="submit" className="btn-primary" style={{ marginTop: '24px' }}>
              Join Room
            </button>
          </form>
        </div>
      </main>
    </div>
  );
}
