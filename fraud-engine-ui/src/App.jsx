/* eslint-disable react-hooks/exhaustive-deps */
import { useEffect, useState, useCallback } from 'react'
import './App.css'

function App() {
  const [transactions, setTransactions] = useState([])
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedTransaction, setSelectedTransaction] = useState(null)

  const fetchTransactions = useCallback(async () => {
    try {
      const res = await fetch('http://localhost:8081/api/transactions')
      const data = await res.json()
      setTransactions(Array.isArray(data) ? data.sort((a, b) => b.id - a.id) : [])
    } catch (err) { console.error("Veri çekme hatası:", err) }
  }, [])

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchTransactions()
    const interval = setInterval(fetchTransactions, 5000)
    return () => clearInterval(interval)
  }, [fetchTransactions])

  const updateStatus = async (id, newStatus) => {
    await fetch(`http://localhost:8081/api/transactions/${id}/status?status=${newStatus}`, { method: 'PUT' })
    fetchTransactions()
  }

  const filtered = transactions.filter(t => t?.account?.accountNumber?.toLowerCase().includes(searchTerm.toLowerCase()))
  
  // SAYAÇ MANTIĞI: Sadece müfettişin henüz dokunmadığı işlemleri sayar
  const pendingCount = transactions.filter(t => 
    t.status === 'FLAGGED' || (t.status === 'BLOCKED' && !t.approvedBy)
  ).length

  return (
    <div>
      <h2 style={{ color: '#fff' }}>🛡️ Fraud Engine | Denetim Paneli</h2>
      
      <div className="stats-container">
        <div className="stat-card">
          <h4>İnceleme Bekleyen</h4>
          <p>{pendingCount} İşlem</p>
        </div>
      </div>

      <input className="search-box" placeholder="🔍 Hesap numarası ara..." onChange={(e) => setSearchTerm(e.target.value)} />
      
      <div className="table-container">
        <table>
          <thead><tr><th>ID</th><th>Gönderen</th><th>Tutar</th><th>Durum</th><th>İz</th><th>İşlemler</th></tr></thead>
          <tbody>
            {filtered.map(t => (
              <tr key={t.id} onClick={() => setSelectedTransaction(t)}>
                <td>#{t.id}</td>
                <td>{t.account?.accountNumber || 'N/A'}</td>
                <td>{t.amount} ₺</td>
                <td><span className={`badge ${t.status.toLowerCase()}`}>{t.status}</span></td>
                <td style={{ fontSize: '0.75rem', lineHeight: '1.2' }}>
                  {t.approvedBy ? (<>{t.approvedBy}<br/><span style={{ color: '#94a3b8' }}>{new Date(t.processedAt).toLocaleTimeString()}</span></>) : '-'}
                </td>
                <td onClick={(e) => e.stopPropagation()}>
                  {(t.status === 'FLAGGED' || (t.status === 'BLOCKED' && !t.approvedBy)) && (
                    <>
                      <button className="btn btn-approve" onClick={() => updateStatus(t.id, 'APPROVED')}>✅ Onayla</button>
                      <button className="btn btn-reject" onClick={() => updateStatus(t.id, 'BLOCKED')}>❌ Reddet</button>
                    </>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {selectedTransaction && (
        <div className="modal-overlay" onClick={() => setSelectedTransaction(null)}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <h3>İşlem Detayları #{selectedTransaction.id}</h3>
            <p><strong>IP Adresi:</strong> {selectedTransaction.ipAddress}</p>
            <p><strong>Hedef Hesap:</strong> {selectedTransaction.targetAccountNumber || 'N/A'}</p>
            <p><strong>İşlem Zamanı:</strong> {new Date(selectedTransaction.processedAt).toLocaleString()}</p>
            <button className="btn btn-approve" onClick={() => setSelectedTransaction(null)}>Kapat</button>
          </div>
        </div>
      )}
    </div>
  )
}
export default App