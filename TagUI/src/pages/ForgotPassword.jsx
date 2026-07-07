import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Send, Mail, ArrowLeft } from 'lucide-react';
import api from '../services/api';

export default function ForgotPassword() {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    
    try {
      await api.post('/auth/reset-password-request', { email });
      alert('Reset code has been sent to your email.');
      navigate('/reset-password', { state: { email } });
    } catch (err) {
      console.error('Forgot password error', err);
      setError(err.response?.data?.message || err.response?.data || err.message || 'Failed to send reset code.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-layout">
      <div className="glass-container animate-fade-in">
        <h2 style={{ textAlign: 'center', marginBottom: '8px' }}>Forgot Password</h2>
        <p className="subtitle" style={{ textAlign: 'center' }}>
          Enter your email address and we'll send you a code to reset your password.
        </p>

        {error && (
          <div style={{ backgroundColor: 'rgba(239, 68, 68, 0.1)', color: 'var(--danger-color)', padding: '12px', borderRadius: '8px', marginBottom: '20px', fontSize: '0.875rem' }}>
            {error}
          </div>
        )}
        
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Email Address</label>
            <div style={{ position: 'relative' }}>
              <Mail style={{ position: 'absolute', top: '12px', left: '12px', color: 'var(--text-secondary)' }} size={20} />
              <input 
                type="email" 
                className="form-input" 
                placeholder="name@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                style={{ width: '100%', paddingLeft: '40px' }}
                required 
              />
            </div>
          </div>

          <button type="submit" className="btn-primary" style={{ marginTop: '24px' }} disabled={loading}>
            {loading ? 'Sending Code...' : (
              <>
                <Send size={20} />
                Send Reset Code
              </>
            )}
          </button>
        </form>

        <div className="auth-footer-center" style={{ marginTop: '32px' }}>
          <Link to="/login" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}>
            <ArrowLeft size={16} />
            Back to Login
          </Link>
        </div>
      </div>
    </div>
  );
}

