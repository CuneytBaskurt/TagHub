import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { LayoutDashboard, Plus, ArrowLeft, Users, Shield, ArrowRight, XCircle, Box, KeyRound, UserMinus, Download } from 'lucide-react';
import api from '../services/api';
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

export default function MyRooms() {
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [newRoomName, setNewRoomName] = useState('');
  const [newRoomCapacity, setNewRoomCapacity] = useState('');
  const [newRoomLabels, setNewRoomLabels] = useState('');
  const [newRoomFile, setNewRoomFile] = useState(null);
  const [creating, setCreating] = useState(false);
  const [createError, setCreateError] = useState(null);
  const [showParticipantsModal, setShowParticipantsModal] = useState(false);
  const [selectedRoomId, setSelectedRoomId] = useState(null);
  const [participants, setParticipants] = useState([]);
  const [loadingParticipants, setLoadingParticipants] = useState(false);

  const navigate = useNavigate();

  const token = localStorage.getItem('token');
  const decoded = parseJwt(token);
  const currentUserId = decoded?.userId;

  useEffect(() => {
    if (!token) {
      navigate('/login');
      return;
    }

    const fetchRooms = async () => {
      try {
        const response = await api.get('/room/my-rooms');
        setRooms(response.data);
      } catch (err) {
        console.error('Failed to fetch rooms:', err);
        setError(err.response?.data?.message || err.message || 'Odalar yüklenirken bir hata oluştu.');
      } finally {
        setLoading(false);
      }
    };

    fetchRooms();
  }, [navigate, token]);

  const handleCloseRoom = async (roomId) => {
    if (!window.confirm("Bu odayı kapatmak (Close Room) istediğinize emin misiniz?")) return;
    
    try {
      try {
        await api.post(`/texts/${roomId}/close`);
      } catch (tallyErr) {
        console.warn("Oylar sayılırken bir sorun oluştu (Belki hiç veri yüklenmemiştir):", tallyErr);
      }
      await api.delete(`/room/${roomId}`);
      setRooms(prevRooms => prevRooms.map(room => 
        room.id === roomId ? { ...room, status: 'CLOSED' } : room
      ));
      alert("Oda başarıyla kapatıldı ve oylar sayıldı.");
    } catch (err) {
      console.error('Failed to close room:', err);
      alert('Oda kapatılırken bir hata oluştu: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleEnterRoom = (roomId) => {
    navigate(`/room/${roomId}`);
  };

  const handleOpenParticipants = async (roomId) => {
    setSelectedRoomId(roomId);
    setShowParticipantsModal(true);
    setLoadingParticipants(true);
    
    try {
      const response = await api.get(`/room/${roomId}/participants`);
      setParticipants(response.data);
    } catch (err) {
      console.error('Failed to fetch participants:', err);
      alert('Kullanıcılar yüklenirken bir hata oluştu: ' + (err.response?.data?.message || err.message));
      setShowParticipantsModal(false);
    } finally {
      setLoadingParticipants(false);
    }
  };

  const handleKickUser = async (targetUserId) => {
    if (!window.confirm("Bu kullanıcıyı odadan atmak istediğinize emin misiniz?")) return;
    
    try {
      await api.post(`/room/${selectedRoomId}/kick/${targetUserId}`);
      setParticipants(prev => prev.filter(p => p.userId !== targetUserId));
      alert("Kullanıcı odadan başarıyla atıldı.");
    } catch (err) {
      console.error('Failed to kick user:', err);
      alert('Kullanıcı atılırken bir hata oluştu: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleDownloadReport = async (roomId) => {
    try {
      const response = await api.get(`/texts/${roomId}/export`, {
        responseType: 'blob'
      });
      
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `dataset_${roomId.substring(0,8)}.csv`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (err) {
      console.error('Download error:', err);
      let errorMessage = '';
      if (err.response?.data instanceof Blob) {
        errorMessage = await err.response.data.text();
      } else {
        errorMessage = err.response?.data?.message || err.response?.data?.error || err.message;
      }
      if (errorMessage.includes("metin bulunamadı") || errorMessage.includes("500") || errorMessage.includes("503") || errorMessage.includes("Service Unavailable") || errorMessage.includes("Internal Server Error")) {
        const confirmTally = window.confirm("Bu odanın oyları arka planda henüz sayılmamış veya hesaplanmamış görünüyor (Odayı eski sürümde kapatmış olabilirsiniz). Şimdi oyları hesaplatıp raporu indirmek ister misiniz?");
        if (confirmTally) {
          try {
            await api.post(`/texts/${roomId}/close`);
            alert("Oylar başarıyla hesaplandı! Lütfen şimdi Download butonuna tekrar basın.");
          } catch (tallyErr) {
            console.error('Tally error:', tallyErr);
            alert('Oyları hesaplarken bir hata oluştu: ' + (tallyErr.response?.data?.message || tallyErr.message));
          }
        }
      } else {
        alert('Rapor indirilirken hata oluştu: ' + errorMessage);
      }
    }
  };

  const handleCreateSubmit = async (e) => {
    e.preventDefault();
    setCreating(true);
    setCreateError(null);
    const labelsArray = newRoomLabels.split(',').map(l => l.trim()).filter(l => l !== '');
    
    try {
      const response = await api.post('/room/create', {
        name: newRoomName,
        capacity: Number(newRoomCapacity),
        labels: labelsArray
      });
      setRooms(prev => [...prev, response.data]);
      if (newRoomFile) {
        try {
          const formData = new FormData();
          formData.append('file', newRoomFile);
          await api.post(`/texts/${response.data.id}/upload-file`, formData, {
            headers: {
              'Content-Type': 'multipart/form-data'
            }
          });
          alert(`Oda ve veri seti başarıyla oluşturuldu! Davet Kodunuz: ${response.data.inviteToken}`);
        } catch (uploadErr) {
          console.error('Dataset upload failed:', uploadErr);
          const uploadErrMsg = typeof uploadErr.response?.data === 'string' 
            ? uploadErr.response.data 
            : (uploadErr.response?.data?.message || uploadErr.message || 'Veri seti yüklenemedi.');
          alert(`Oda oluşturuldu (Davet Kodu: ${response.data.inviteToken}) FAKAT veri seti yüklenirken hata oluştu: ${uploadErrMsg}`);
        }
      } else {
        alert(`Oda başarıyla oluşturuldu! Davet Kodunuz: ${response.data.inviteToken}`);
      }
      setNewRoomName('');
      setNewRoomCapacity('');
      setNewRoomLabels('');
      setNewRoomFile(null);
      setShowModal(false);
      
    } catch (err) {
      console.error('Failed to create room:', err);
      const errMsg = typeof err.response?.data === 'string' 
        ? err.response.data 
        : (err.response?.data?.message || err.response?.data?.error || err.message || 'Oda oluşturulamadı.');
      setCreateError(errMsg);
    } finally {
      setCreating(false);
    }
  };

  if (loading) {
    return <div className="auth-layout"><p>Odalar yükleniyor...</p></div>;
  }
  const activeRooms = rooms.filter(r => r.status === 'ACTIVE');
  const closedRooms = rooms.filter(r => r.status === 'CLOSED');
  const renderRoomCard = (room, isActive) => {
    const isAdmin = room.owner_id === currentUserId;
    const roleText = isAdmin ? 'ADMIN' : 'USER';
    
    return (
      <div key={room.id} style={{ 
        background: 'rgba(0,0,0,0.2)', 
        border: `1px solid ${isActive ? 'rgba(99, 102, 241, 0.3)' : 'var(--border-color)'}`, 
        borderRadius: '12px', 
        padding: '20px',
        marginBottom: '16px',
        display: 'flex',
        flexDirection: 'column',
        gap: '16px',
        transition: 'all 0.3s ease'
      }}>
        
        
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
          <div>
            <h3 style={{ margin: 0, fontSize: '1.2rem', color: 'var(--text-primary)' }}>{room.name}</h3>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '4px', display: 'flex', alignItems: 'center', gap: '4px' }}>
              <Box size={14}/> ID: {room.id.substring(0,8)}...
            </div>
            {room.inviteToken && (
               <div style={{ fontSize: '0.85rem', color: 'var(--primary-color)', marginTop: '4px', display: 'flex', alignItems: 'center', gap: '4px', fontWeight: '500' }}>
                 <KeyRound size={14}/> Token: {room.inviteToken}
               </div>
            )}
          </div>
          <div style={{ 
            padding: '4px 10px', 
            borderRadius: '20px', 
            fontSize: '0.75rem', 
            fontWeight: '600',
            backgroundColor: isActive ? 'rgba(34, 197, 94, 0.2)' : 'rgba(239, 68, 68, 0.2)',
            color: isActive ? '#4ade80' : '#f87171'
          }}>
            {room.status}
          </div>
        </div>

        
        <div style={{ display: 'flex', gap: '20px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.9rem', color: 'var(--text-secondary)' }}>
            <Users size={16} /> Capacity: <strong style={{ color: 'var(--text-primary)' }}>{room.capacity}</strong>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.9rem', color: 'var(--text-secondary)' }}>
            <Shield size={16} color={isAdmin ? 'var(--primary-color)' : 'var(--text-secondary)'} /> Role: <strong style={{ color: isAdmin ? 'var(--primary-color)' : 'var(--text-primary)' }}>{roleText}</strong>
          </div>
        </div>

        
        {room.labels && room.labels.length > 0 && (
          <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
            {room.labels.map((lbl, idx) => (
              <span key={idx} style={{ background: 'rgba(255,255,255,0.1)', padding: '2px 8px', borderRadius: '4px', fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                {lbl}
              </span>
            ))}
          </div>
        )}

        
        <div style={{ display: 'flex', gap: '12px', marginTop: '4px', flexWrap: 'wrap' }}>
          {isActive && (
            <button onClick={() => handleEnterRoom(room.id)} className="btn-primary" style={{ padding: '8px 16px', flex: 1, minWidth: '120px' }}>
              Enter Room <ArrowRight size={16} />
            </button>
          )}
          {isAdmin && isActive && (
            <>
              <button onClick={() => handleOpenParticipants(room.id)} className="btn-primary" style={{ padding: '8px 16px', flex: 1, minWidth: '100px', backgroundColor: 'rgba(99, 102, 241, 0.2)', color: 'var(--primary-color)' }}>
                Users <Users size={16} />
              </button>
              <button onClick={() => handleCloseRoom(room.id)} className="btn-primary" style={{ padding: '8px 16px', flex: 1, minWidth: '120px', backgroundColor: 'rgba(239, 68, 68, 0.1)', color: 'var(--danger-color)' }}>
                Close Room <XCircle size={16} />
              </button>
            </>
          )}
          {isAdmin && (
            <button onClick={() => handleDownloadReport(room.id)} className="btn-primary" style={{ padding: '8px 16px', flex: 1, minWidth: '120px', backgroundColor: 'rgba(34, 197, 94, 0.2)', color: '#4ade80' }}>
              Download <Download size={16} />
            </button>
          )}
        </div>
      </div>
    );
  };

  return (
    <div style={{ minHeight: '100vh', padding: '40px', display: 'flex', flexDirection: 'column', alignItems: 'center', position: 'relative' }}>
      
      
      <div style={{ width: '100%', maxWidth: '900px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '32px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <LayoutDashboard size={32} color="var(--primary-color)" />
          <h1 style={{ margin: 0 }}>My Rooms</h1>
        </div>
        
        <div style={{ display: 'flex', gap: '16px' }}>
          <Link to="/dashboard" className="btn-primary" style={{ background: 'rgba(255,255,255,0.1)', color: 'var(--text-primary)', width: 'auto', padding: '10px 20px', textDecoration: 'none' }}>
            <ArrowLeft size={18} />
            Dashboard
          </Link>
          <button onClick={() => setShowModal(true)} className="btn-primary" style={{ width: 'auto', padding: '10px 20px' }}>
            <Plus size={18} />
            Create Room
          </button>
        </div>
      </div>

      {error && (
        <div style={{ width: '100%', maxWidth: '900px', backgroundColor: 'rgba(239, 68, 68, 0.1)', color: 'var(--danger-color)', padding: '16px', borderRadius: '8px', marginBottom: '24px' }}>
          {error}
        </div>
      )}

      
      <div style={{ width: '100%', maxWidth: '900px', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '32px' }}>
        
        
        <div className="glass-container" style={{ padding: '24px', margin: 0, maxWidth: 'none', alignSelf: 'start' }}>
          <h3 style={{ marginBottom: '20px', borderBottom: '1px solid var(--border-color)', paddingBottom: '12px', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <div style={{ width: '10px', height: '10px', borderRadius: '50%', backgroundColor: '#4ade80' }}></div>
            ACTIVE ROOMS ({activeRooms.length})
          </h3>
          
          {activeRooms.length === 0 ? (
            <p style={{ color: 'var(--text-secondary)', fontStyle: 'italic' }}>Hiç aktif odanız bulunmuyor.</p>
          ) : (
            activeRooms.map(room => renderRoomCard(room, true))
          )}
        </div>

        
        <div className="glass-container" style={{ padding: '24px', margin: 0, maxWidth: 'none', alignSelf: 'start', opacity: 0.8 }}>
          <h3 style={{ marginBottom: '20px', borderBottom: '1px solid var(--border-color)', paddingBottom: '12px', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <div style={{ width: '10px', height: '10px', borderRadius: '50%', backgroundColor: '#f87171' }}></div>
            CLOSED ROOMS ({closedRooms.length})
          </h3>
          
          {closedRooms.length === 0 ? (
            <p style={{ color: 'var(--text-secondary)', fontStyle: 'italic' }}>Geçmişte kapanmış odanız bulunmuyor.</p>
          ) : (
            closedRooms.map(room => renderRoomCard(room, false))
          )}
        </div>
      </div>

      
      {showParticipantsModal && (
        <div style={{
          position: 'fixed',
          top: 0, left: 0, right: 0, bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.7)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000,
          padding: '20px'
        }}>
          <div className="glass-container animate-fade-in" style={{ maxWidth: '500px', width: '100%', margin: 0, maxHeight: '80vh', display: 'flex', flexDirection: 'column' }}>
            <h2 style={{ marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Users size={24} color="var(--primary-color)" />
              Oda Kullanıcıları
            </h2>
            
            {loadingParticipants ? (
              <p style={{ color: 'var(--text-secondary)' }}>Kullanıcılar yükleniyor...</p>
            ) : (
              <div style={{ overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '12px', paddingRight: '4px', flex: 1 }}>
                {participants.length === 0 ? (
                  <p style={{ color: 'var(--text-secondary)' }}>Bu odada başka kullanıcı bulunmuyor.</p>
                ) : (
                  participants.map(p => (
                    <div key={p.userId} style={{ 
                      background: 'rgba(0,0,0,0.2)', 
                      border: '1px solid var(--border-color)', 
                      borderRadius: '8px', 
                      padding: '16px',
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center'
                    }}>
                      <div>
                        <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>User ID:</div>
                        <div style={{ fontSize: '1rem', fontWeight: '500', color: 'var(--text-primary)' }}>{p.userId.substring(0,8)}...</div>
                        <div style={{ display: 'flex', gap: '8px', marginTop: '6px' }}>
                          <span style={{ fontSize: '0.75rem', background: 'rgba(99,102,241,0.2)', color: 'var(--primary-color)', padding: '2px 6px', borderRadius: '4px' }}>{p.role}</span>
                          <span style={{ fontSize: '0.75rem', background: 'rgba(34,197,94,0.2)', color: '#4ade80', padding: '2px 6px', borderRadius: '4px' }}>{p.status}</span>
                        </div>
                      </div>

                      {p.userId !== currentUserId && (
                        <button onClick={() => handleKickUser(p.userId)} className="btn-primary" style={{ width: 'auto', padding: '8px 12px', backgroundColor: 'rgba(239,68,68,0.1)', color: 'var(--danger-color)', border: '1px solid rgba(239,68,68,0.3)' }}>
                          <UserMinus size={16} />
                          Kick
                        </button>
                      )}
                    </div>
                  ))
                )}
              </div>
            )}

            <button onClick={() => setShowParticipantsModal(false)} className="btn-primary" style={{ marginTop: '24px', backgroundColor: 'transparent', border: '1px solid var(--border-color)', color: 'var(--text-primary)' }}>
              Kapat
            </button>
          </div>
        </div>
      )}

      
      {showModal && (
        <div style={{
          position: 'fixed',
          top: 0, left: 0, right: 0, bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.7)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000,
          padding: '20px'
        }}>
          <div className="glass-container animate-fade-in" style={{ maxWidth: '400px', width: '100%', margin: 0 }}>
            <h2 style={{ marginBottom: '16px' }}>Create New Room</h2>
            <p className="subtitle" style={{ marginBottom: '24px' }}>Fill in the details to create a new labeling room.</p>
            
            {createError && (
              <div style={{ backgroundColor: 'rgba(239, 68, 68, 0.1)', color: 'var(--danger-color)', padding: '12px', borderRadius: '8px', marginBottom: '20px', fontSize: '0.875rem' }}>
                {createError}
              </div>
            )}

            <form onSubmit={handleCreateSubmit}>
              <div className="form-group">
                <label>Room Name</label>
                <input 
                  type="text" 
                  className="form-input" 
                  placeholder="e.g. Image Classification Project"
                  value={newRoomName}
                  onChange={(e) => setNewRoomName(e.target.value)}
                  required 
                />
              </div>
              
              <div className="form-group">
                <label>Capacity</label>
                <input 
                  type="number" 
                  className="form-input" 
                  placeholder="e.g. 5"
                  value={newRoomCapacity}
                  onChange={(e) => setNewRoomCapacity(e.target.value)}
                  min="1"
                  required 
                />
              </div>

              <div className="form-group">
                <label>Labels (Comma separated)</label>
                <input 
                  type="text" 
                  className="form-input" 
                  placeholder="e.g. Cat, Dog, Bird"
                  value={newRoomLabels}
                  onChange={(e) => setNewRoomLabels(e.target.value)}
                />
              </div>

              <div className="form-group">
                <label>Dataset File (Optional)</label>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginBottom: '8px', lineHeight: '1.4' }}>
                  Format: <strong>.txt</strong> veya <strong>.csv</strong>. Dosyadaki her bir satır sistem tarafından ayrı bir etiketlenecek veri (metin) olarak kabul edilir.
                </div>
                <input 
                  type="file" 
                  accept=".txt,.csv"
                  className="form-input" 
                  onChange={(e) => setNewRoomFile(e.target.files[0])}
                  style={{ padding: '8px' }}
                />
              </div>

              <div style={{ display: 'flex', gap: '12px', marginTop: '32px' }}>
                <button type="button" onClick={() => setShowModal(false)} className="btn-primary" style={{ flex: 1, backgroundColor: 'transparent', border: '1px solid var(--border-color)', color: 'var(--text-primary)' }}>
                  Cancel
                </button>
                <button type="submit" className="btn-primary" style={{ flex: 1 }} disabled={creating}>
                  {creating ? 'Creating...' : 'Create'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

    </div>
  );
}

