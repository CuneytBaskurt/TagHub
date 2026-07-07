import React, { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { KeyRound, Lock, ArrowLeft, CheckCircle2 } from 'lucide-react';
import api from '../services/api';

export default function ResetPassword() {
  const [code, setCode] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);
  
  const location = useLocation();
  const navigate = useNavigate();
  // We passed email via state from ForgotPassword page
  const email = location.state?.email || '';

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!email) {
      setError('Email is missing. Please go back and request a new code.');
      return;
    }

    setLoading(true);
    setError(null);
    
    try {
      // DTO in AuthController is PasswordVerifyRequest(String email, String code, String newPassword)
      await api.post('/auth/verify-reset-code', { email, code, newPassword });
      setSuccess(true);
    } catch (err) {
      console.error('Reset password error', err);
      setError(err.response?.data?.message || err.response?.data || err.message || 'Failed to reset password.');
    } finally {
      setLoading(false);
    }
  };

  if (success) {
    return (
      <div className="auth-layout">
        <div className="glass-container animate-fade-in" style={{ textAlign: 'center' }}>
          <CheckCircle2 size={64} color="var(--primary-color)" style={{ margin: '0 auto 24px auto' }} />
          <h2 style={{ marginBottom: '16px' }}>Password Reset Successfully</h2>
          <p className="subtitle">Your password has been changed. You can now login with your new credentials.</p>
          <button onClick={() => navigate('/login')} className="btn-primary">
            Go to Login
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="auth-layout">
      <div className="glass-container animate-fade-in">
        <h2 style={{ textAlign: 'center', marginBottom: '8px' }}>Set New Password</h2>
        <p className="subtitle" style={{ textAlign: 'center' }}>
          {email ? (
            <>We've sent a verification code to <strong style={{ color: 'var(--text-primary)' }}>{email}</strong></>
          ) : (
            'Please enter the verification code sent to your email.'
          )}
        </p>
        
        {error && (
          <div style={{ backgroundColor: 'rgba(239, 68, 68, 0.1)', color: 'var(--danger-color)', padding: '12px', borderRadius: '8px', marginBottom: '20px', fontSize: '0.875rem' }}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Verification Code</label>
            <div style={{ position: 'relative' }}>
              <KeyRound style={{ position: 'absolute', top: '12px', left: '12px', color: 'var(--text-secondary)' }} size={20} />
              <input 
                type="text" 
                className="form-input" 
                placeholder="Enter reset code"
                value={code}
                onChange={(e) => setCode(e.target.value)}
                style={{ width: '100%', paddingLeft: '40px', letterSpacing: '2px' }}
                required 
              />
            </div>
          </div>

          <div className="form-group">
            <label>New Password</label>
            <div style={{ position: 'relative' }}>
              <Lock style={{ position: 'absolute', top: '12px', left: '12px', color: 'var(--text-secondary)' }} size={20} />
              <input 
                type="password" 
                className="form-input" 
                placeholder="••••••••"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                style={{ width: '100%', paddingLeft: '40px' }}
                required 
              />
            </div>
          </div>

          <button type="submit" className="btn-primary" style={{ marginTop: '32px' }} disabled={loading}>
            {loading ? 'Resetting...' : (
              <>
                <CheckCircle2 size={20} />
                Reset Password
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
