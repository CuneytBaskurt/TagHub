import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { ArrowLeft, CheckCircle2, Tag, Loader2, Sparkles } from 'lucide-react';
import api from '../services/api';

export default function LabelingWorkspace() {
  const { roomId } = useParams();
  const navigate = useNavigate();

  const [room, setRoom] = useState(null);
  const [batch, setBatch] = useState([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [sessionCount, setSessionCount] = useState(0);
  
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [isDone, setIsDone] = useState(false);

  useEffect(() => {
    const initWorkspace = async () => {
      try {
        setLoading(true);
        // 1. Odanın detaylarını al (Etiketleri öğrenmek için)
        const roomRes = await api.get(`/room/${roomId}`);
        setRoom(roomRes.data);

        // 2. İlk batch'i çek
        await fetchNextBatch();
      } catch (err) {
        console.error('Workspace init error', err);
        setError(err.response?.data?.message || 'Çalışma alanı yüklenirken hata oluştu.');
        setLoading(false);
      }
    };

    initWorkspace();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [roomId]);

  const fetchNextBatch = async () => {
    try {
      const textsRes = await api.get(`/texts/${roomId}/next-batch?batchSize=10`);
      if (textsRes.data && textsRes.data.length > 0) {
        setBatch(textsRes.data);
        setCurrentIndex(0);
      } else {
        setIsDone(true);
      }
    } catch (err) {
      console.error('Fetch batch error', err);
      setError(err.response?.data?.message || 'Veriler çekilirken hata oluştu.');
    } finally {
      setLoading(false);
    }
  };

  const handleVote = async (labelIndex) => {
    if (submitting) return;
    
    const currentText = batch[currentIndex];
    setSubmitting(true);
    
    try {
      await api.post(`/annotations/${roomId}/vote`, {
        textId: currentText.id,
        labelIndex: labelIndex
      });
      
      setSessionCount(prev => prev + 1);
      
      // Sonraki cümleye geç
      if (currentIndex + 1 < batch.length) {
        setCurrentIndex(prev => prev + 1);
        setSubmitting(false);
      } else {
        // Mevcut batch bitti, yenisini çek
        setLoading(true);
        await fetchNextBatch();
        setSubmitting(false);
      }
      
    } catch (err) {
      console.error('Vote error', err);
      alert('Etiket kaydedilirken hata oluştu: ' + (err.response?.data?.message || err.message));
      setSubmitting(false);
    }
  };

  if (loading && batch.length === 0 && !isDone && !error) {
    return (
      <div className="auth-layout" style={{ display: 'flex', flexDirection: 'column', gap: '16px', alignItems: 'center' }}>
        <Loader2 size={48} className="animate-spin" color="var(--primary-color)" />
        <p>Veriler hazırlanıyor...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="auth-layout">
        <div className="glass-container" style={{ textAlign: 'center' }}>
          <h2 style={{ color: 'var(--danger-color)', marginBottom: '16px' }}>Bir Hata Oluştu</h2>
          <p>{error}</p>
          <Link to="/my-rooms" className="btn-primary" style={{ marginTop: '20px', textDecoration: 'none', display: 'inline-flex', width: 'auto' }}>Geri Dön</Link>
        </div>
      </div>
    );
  }

  if (isDone) {
    return (
      <div className="auth-layout">
        <div className="glass-container animate-fade-in" style={{ textAlign: 'center' }}>
          <CheckCircle2 size={64} color="#4ade80" style={{ margin: '0 auto 24px auto' }} />
          <h2 style={{ marginBottom: '12px' }}>Tebrikler!</h2>
          <p className="subtitle">Odadaki etiketlenecek tüm verileri bitirdiniz.</p>
          <div style={{ margin: '24px 0', fontSize: '1.2rem' }}>
            Bu oturumda toplam <strong style={{ color: 'var(--primary-color)' }}>{sessionCount}</strong> veri etiketlediniz.
          </div>
          <Link to="/my-rooms" className="btn-primary" style={{ textDecoration: 'none', display: 'inline-flex', width: 'auto' }}>Odalarıma Dön</Link>
        </div>
      </div>
    );
  }

  const currentText = batch[currentIndex];

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', alignItems: 'center', padding: '40px 20px' }}>
      
      {/* Header */}
      <div style={{ width: '100%', maxWidth: '800px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <Link to="/my-rooms" style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--text-secondary)', textDecoration: 'none', fontWeight: '500' }}>
          <ArrowLeft size={20} /> Odalara Dön
        </Link>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', background: 'rgba(255,255,255,0.05)', padding: '8px 16px', borderRadius: '20px' }}>
          <Tag size={16} color="var(--primary-color)" />
          <span style={{ fontWeight: '600', fontSize: '0.9rem' }}>{room?.name}</span>
        </div>
      </div>

      {/* Progress Bar (Session based) */}
      <div style={{ width: '100%', maxWidth: '800px', marginBottom: '40px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px', fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
          <span style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <Sparkles size={14} color="#f59e0b" /> Oturum İlerlemesi
          </span>
          <span><strong>{sessionCount}</strong> Veri Etiketlendi</span>
        </div>
        <div style={{ width: '100%', height: '8px', backgroundColor: 'rgba(255,255,255,0.1)', borderRadius: '4px', overflow: 'hidden', position: 'relative' }}>
          <div style={{ 
            position: 'absolute',
            left: 0, top: 0, bottom: 0,
            width: '100%', 
            backgroundColor: 'var(--primary-color)',
            opacity: 0.3
          }}></div>
          <div style={{ 
            position: 'absolute',
            left: 0, top: 0, bottom: 0,
            // Visual fake progress that pulses or just stays full to indicate continuous activity
            width: '100%', 
            backgroundColor: 'var(--primary-color)',
            transformOrigin: 'left',
            animation: 'pulse 2s infinite ease-in-out'
          }}></div>
        </div>
      </div>

      {/* Labeling Workspace */}
      <div className="glass-container animate-fade-in" style={{ width: '100%', maxWidth: '800px', padding: '40px', margin: 0 }}>
        <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '16px', display: 'flex', justifyContent: 'space-between' }}>
          <span>Veri ID: {currentText?.id.substring(0,8)}...</span>
          <span>Paket: {currentIndex + 1} / {batch.length}</span>
        </div>
        
        {/* The Text to Label */}
        <div style={{ 
          fontSize: '1.25rem', 
          lineHeight: '1.6', 
          fontWeight: '500', 
          color: 'var(--text-primary)',
          textAlign: 'center',
          minHeight: '160px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          backgroundColor: 'rgba(0,0,0,0.2)',
          padding: '32px',
          borderRadius: '12px',
          border: '1px solid var(--border-color)',
          marginBottom: '32px',
          position: 'relative'
        }}>
          {loading || submitting ? (
            <Loader2 size={32} className="animate-spin" color="var(--primary-color)" />
          ) : (
            currentText?.content
          )}
        </div>

        {/* Labels / Buttons */}
        <div>
          <h4 style={{ textAlign: 'center', marginBottom: '20px', color: 'var(--text-secondary)', fontWeight: '500' }}>Bu metin için uygun etiketi seçin:</h4>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '16px', justifyContent: 'center' }}>
            {room?.labels && room.labels.length > 0 ? (
              room.labels.map((label, index) => (
                <button 
                  key={index}
                  onClick={() => handleVote(index)}
                  disabled={submitting || loading}
                  style={{ 
                    width: 'auto', 
                    padding: '14px 28px',
                    backgroundColor: 'rgba(99, 102, 241, 0.1)',
                    border: '1px solid var(--primary-color)',
                    borderRadius: '8px',
                    color: 'var(--text-primary)',
                    fontWeight: '600',
                    fontSize: '1.05rem',
                    transition: 'all 0.2s ease',
                    cursor: (submitting || loading) ? 'not-allowed' : 'pointer',
                    opacity: (submitting || loading) ? 0.6 : 1
                  }}
                  onMouseOver={(e) => {
                    if(!submitting && !loading) {
                      e.currentTarget.style.backgroundColor = 'var(--primary-color)';
                      e.currentTarget.style.color = '#fff';
                      e.currentTarget.style.transform = 'translateY(-2px)';
                    }
                  }}
                  onMouseOut={(e) => {
                    if(!submitting && !loading) {
                      e.currentTarget.style.backgroundColor = 'rgba(99, 102, 241, 0.1)';
                      e.currentTarget.style.color = 'var(--text-primary)';
                      e.currentTarget.style.transform = 'translateY(0)';
                    }
                  }}
                >
                  {label}
                </button>
              ))
            ) : (
              <p style={{ color: 'var(--danger-color)' }}>Bu odada tanımlanmış hiçbir etiket yok!</p>
            )}
          </div>
        </div>

      </div>
    </div>
  );
}
