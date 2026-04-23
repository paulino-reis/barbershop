import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { Calendar, Clock, User, Scissors, Menu, X, Edit2, Trash2, Check } from 'lucide-react';
import axios from 'axios';
import Navigation from '../components/Navigation';

const Agendamento = () => {
  const { user, logout } = useAuth();
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [selectedProfissional, setSelectedProfissional] = useState('');
  const [selectedServico, setSelectedServico] = useState('');
  const [selectedHorario, setSelectedHorario] = useState('');
  const [profissionais, setProfissionais] = useState([]);
  const [servicos, setServicos] = useState([]);
  const [horariosDisponiveis, setHorariosDisponiveis] = useState([]);
  const [horariosOcupados, setHorariosOcupados] = useState([]);
  const [agendamentos, setAgendamentos] = useState([]);
  const [showMobileMenu, setShowMobileMenu] = useState(false);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [showCancelConfirm, setShowCancelConfirm] = useState(false);
  const [cancelingId, setCancelingId] = useState(null);
  const [filtroStatus, setFiltroStatus] = useState('AGENDADO');

  const carregarDadosIniciais = async () => {
    try {
      const [profissionaisRes, servicosRes] = await Promise.all([
        axios.get('/api/profissionais'),
        axios.get('/api/servicos')
      ]);

      setProfissionais(profissionaisRes.data);
      setServicos(servicosRes.data);
    } catch (error) {
      setMessage('Erro ao carregar profissionais e serviços');
    }

    // Load agendamentos separately
    try {
      const agendamentosRes = await axios.get('/api/agendamentos');
      const agendamentosOrdenados = agendamentosRes.data.sort((a, b) => 
        new Date(a.dataAgendamento) - new Date(b.dataAgendamento)
      );
      setAgendamentos(agendamentosOrdenados);
    } catch (error) {
      // Don't show error for agendamentos - it might just be that user is not logged in
      console.log('Não foi possível carregar agendamentos:', error.message);
    }
  };

  const carregarHorariosDisponiveis = useCallback(async () => {
    try {
      const dataParam = selectedDate.toISOString();
      const profissionalId = selectedProfissional ? selectedProfissional : null;
      
      const url = profissionalId 
        ? `/api/agendamentos/horarios-disponiveis?data=${dataParam}&profissionalId=${profissionalId}`
        : `/api/agendamentos/horarios-disponiveis?data=${dataParam}`;
      
      const response = await axios.get(url);
      setHorariosDisponiveis(response.data);
    } catch (error) {
      setMessage('Erro ao carregar horários disponíveis');
    }
  }, [selectedDate, selectedProfissional]);

  const carregarHorariosOcupados = useCallback(async () => {
    try {
      const dataParam = selectedDate.toISOString();
      const profissionalId = selectedProfissional ? selectedProfissional : null;
      
      const url = profissionalId 
        ? `/api/agendamentos/horarios-ocupados?data=${dataParam}&profissionalId=${profissionalId}`
        : `/api/agendamentos/horarios-ocupados?data=${dataParam}`;
      
      const response = await axios.get(url);
      setHorariosOcupados(response.data);
    } catch (error) {
      setMessage('Erro ao carregar horários ocupados');
    }
  }, [selectedDate, selectedProfissional]);

  useEffect(() => {
    carregarDadosIniciais();
  }, []);

  useEffect(() => {
    if (selectedDate && selectedProfissional) {
      carregarHorariosDisponiveis();
      carregarHorariosOcupados();
    }
  }, [selectedDate, selectedProfissional, carregarHorariosDisponiveis, carregarHorariosOcupados]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!selectedServico || !selectedHorario) {
      setMessage('Selecione um serviço e um horário');
      return;
    }

    setLoading(true);
    setMessage('');

    try {
      const profissionalId = selectedProfissional;

      if (!profissionalId) {
        setMessage('Selecione um profissional');
        return;
      }

      const agendamentoData = {
        profissional: { id: profissionalId },
        servico: { id: selectedServico },
        dataAgendamento: selectedDate.toISOString(),
        horarioAgendado: selectedHorario
      };

      const response = await axios.post('/api/agendamentos', agendamentoData);
      setMessage('Agendamento realizado com sucesso!');
      
      // Limpar formulário
      setSelectedServico('');
      setSelectedHorario('');
      
      // Recarregar horários disponíveis
      await carregarHorariosDisponiveis();
      
      // Recarregar agendamentos
      const agendamentosRes = await axios.get('/api/agendamentos');
      const agendamentosOrdenados = agendamentosRes.data.sort((a, b) => 
        new Date(a.dataAgendamento) - new Date(b.dataAgendamento)
      );
      setAgendamentos(agendamentosOrdenados);
      
      // Confirmar agendamento e abrir WhatsApp
      if (response.data.id) {
        const confirmResponse = await axios.post(`/api/agendamentos/${response.data.id}/confirmar`);
        if (confirmResponse.data.whatsappLink) {
          window.open(confirmResponse.data.whatsappLink, '_blank');
        }
      }
      
    } catch (error) {
      setMessage(error.response?.data?.message || 'Erro ao realizar agendamento');
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = (agendamento) => {
    setSelectedDate(new Date(agendamento.dataAgendamento));
    setSelectedProfissional(agendamento.profissional?.id || '');
    setSelectedServico(agendamento.servico?.id || '');
    setSelectedHorario(agendamento.horarioAgendado || '');
  };

  const handleCancel = async (id) => {
    setCancelingId(id);
    setShowCancelConfirm(true);
  };

  const confirmCancel = async () => {
    try {
      await axios.post(`/api/agendamentos/${cancelingId}/cancelar`);
      setMessage('Agendamento cancelado com sucesso!');
      setShowCancelConfirm(false);
      setCancelingId(null);
      // Recarregar agendamentos
      const agendamentosRes = await axios.get('/api/agendamentos');
      const agendamentosOrdenados = agendamentosRes.data.sort((a, b) => 
        new Date(a.dataAgendamento) - new Date(b.dataAgendamento)
      );
      setAgendamentos(agendamentosOrdenados);
    } catch (error) {
      setMessage(error.response?.data?.message || 'Erro ao cancelar agendamento');
    }
  };

  const handleConfirm = async (id) => {
    try {
      const response = await axios.post(`/api/agendamentos/${id}/confirmar`);
      setMessage('Agendamento confirmado com sucesso!');
      // Recarregar agendamentos
      const agendamentosRes = await axios.get('/api/agendamentos');
      const agendamentosOrdenados = agendamentosRes.data.sort((a, b) => 
        new Date(a.dataAgendamento) - new Date(b.dataAgendamento)
      );
      setAgendamentos(agendamentosOrdenados);
      // Open WhatsApp link if available
      if (response.data.whatsappLink) {
        window.open(response.data.whatsappLink, '_blank');
      }
    } catch (error) {
      setMessage(error.response?.data?.message || 'Erro ao confirmar agendamento');
    }
  };

  const formatarData = (dataString) => {
    const data = new Date(dataString);
    return data.toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  };

  return (
    <div 
      className="min-h-screen"
      style={{
        backgroundImage: 'url(https://images.unsplash.com/photo-1585747860715-2ba37e788b70?auto=format&fit=crop&w=1920&q=80)',
        backgroundSize: 'cover',
        backgroundPosition: 'center',
        backgroundRepeat: 'no-repeat',
        position: 'relative'
      }}
    >
      <div style={{
        position: 'absolute',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundColor: 'rgba(0, 0, 0, 0.6)',
        zIndex: 0
      }}></div>
      <div style={{ position: 'relative', zIndex: 1 }}>
      <Navigation />
      {/* Header */}
      <header 
        className="shadow-sm border-b"
        style={{
          background: 'rgba(30, 41, 59, 0.8)',
          borderBottomColor: 'rgba(71, 85, 105, 0.5)'
        }}
      >
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center">
              <Scissors className="h-8 w-8 text-primary-400 mr-3" />
              <h1 className="text-xl font-semibold text-white">Talison Barbearia</h1>
            </div>
            
            {/* Mobile menu button */}
            <div className="md:hidden">
              <button
                onClick={() => setShowMobileMenu(!showMobileMenu)}
                className="p-2 rounded-md text-gray-400 hover:text-white hover:bg-slate-700 transition-colors"
              >
                {showMobileMenu ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
              </button>
            </div>

            {/* Desktop menu */}
            <div className="hidden md:flex items-center space-x-4">
              <span className="text-gray-300">Bem-vindo, {user?.login}</span>
            </div>
          </div>

          {/* Mobile menu */}
          {showMobileMenu && (
            <div className="md:hidden py-4 border-t" style={{borderTopColor: 'rgba(71, 85, 105, 0.5)'}}>
              <div className="flex flex-col space-y-2">
                <span className="text-gray-300 px-3 py-2">Bem-vindo, {user?.login}</span>
              </div>
            </div>
          )}
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 gap-8">
          {/* Formulário de Agendamento */}
          <div>
            <div 
              className="rounded-lg shadow-lg p-6"
              style={{
                background: 'rgba(255, 255, 255, 0.1)',
                backdropFilter: 'blur(10px)',
                border: '1px solid rgba(255, 255, 255, 0.2)'
              }}
            >
              <h2 className="text-2xl font-bold text-white mb-6">Novo Agendamento</h2>
              
              {message && (
                <div className={`mb-4 p-3 rounded-lg ${
                  message.includes('sucesso') 
                    ? 'bg-green-900/30 text-green-300 border border-green-500/50'
                    : 'bg-red-900/30 text-red-300 border border-red-500/50'
                }`}>
                  {message}
                </div>
              )}

              <form onSubmit={handleSubmit} className="space-y-6">
                {/* Calendário, Profissional e Serviço */}
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                  {/* Calendário */}
                  <div>
                    <label className="block text-sm font-medium text-gray-300 mb-2">
                      <Calendar className="inline h-4 w-4 mr-1" />
                      Data do Agendamento
                    </label>
                    <input
                      type="date"
                      value={selectedDate.toLocaleDateString('en-CA')}
                      onChange={(e) => setSelectedDate(new Date(e.target.value + 'T00:00:00'))}
                      min={new Date().toLocaleDateString('en-CA')}
                      className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all"
                      required
                    />
                  </div>

                  {/* Profissional */}
                  <div>
                    <label className="block text-sm font-medium text-gray-300 mb-2">
                      <User className="inline h-4 w-4 mr-1" />
                      Profissional
                    </label>
                    <select
                      value={selectedProfissional}
                      onChange={(e) => setSelectedProfissional(e.target.value)}
                      className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all"
                      required
                    >
                      <option value="">Selecione um profissional</option>
                      {profissionais.map(profissional => (
                        <option key={profissional.id} value={profissional.id}>
                          {profissional.nome}
                        </option>
                      ))}
                    </select>
                  </div>

                  {/* Serviço */}
                  <div>
                    <label className="block text-sm font-medium text-gray-300 mb-2">
                      <Scissors className="inline h-4 w-4 mr-1" />
                      Serviço
                    </label>
                    <select
                      value={selectedServico}
                      onChange={(e) => setSelectedServico(e.target.value)}
                      className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all"
                      required
                    >
                      <option value="">Selecione um serviço</option>
                      {servicos.map(servico => (
                        <option key={servico.id} value={servico.id}>
                          {servico.nome} - R$ {servico.preco.toFixed(2)}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>

                {/* Horários Disponíveis */}
                <div>
                  <label className="block text-sm font-medium text-gray-300 mb-2">
                    <Clock className="inline h-4 w-4 mr-1" />
                    Horários Disponíveis
                  </label>
                  <div className="grid grid-cols-3 sm:grid-cols-4 gap-2">
                    {(() => {
                      const todosHorarios = [];
                      for (let hora = 9; hora < 21; hora++) {
                        todosHorarios.push(`${String(hora).padStart(2, '0')}:00`);
                        todosHorarios.push(`${String(hora).padStart(2, '0')}:30`);
                      }
                      return todosHorarios.map(horario => {
                        const isDisponivel = horariosDisponiveis.includes(horario);
                        const horarioOcupado = horariosOcupados.find(h => h.horario === horario);
                        return (
                          <button
                            key={horario}
                            type="button"
                            onClick={() => isDisponivel && setSelectedHorario(horario)}
                            disabled={!isDisponivel}
                            title={!isDisponivel && horarioOcupado 
                              ? `Agendado por: ${horarioOcupado.nomeUsuario} - ${horarioOcupado.telefoneUsuario}` 
                              : ''}
                            className={`p-2 text-sm rounded border transition-colors ${
                              selectedHorario === horario
                                ? 'bg-primary-600 text-white border-primary-600'
                                : isDisponivel
                                  ? 'bg-slate-800/50 text-gray-300 border-slate-600 hover:bg-slate-700'
                                  : 'bg-red-900/30 text-red-400 border-red-500/50 cursor-not-allowed'
                            }`}
                          >
                            {horario}
                          </button>
                        );
                      });
                    })()}
                  </div>
                </div>

                <button
                  type="submit"
                  disabled={loading}
                  className="w-full py-3 px-4 bg-gradient-to-r from-primary-600 to-primary-500 hover:from-primary-700 hover:to-primary-600 text-white font-semibold rounded-lg focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50 transition-all transform hover:scale-[1.02] disabled:transform-none shadow-lg"
                >
                  {loading ? 'Agendando...' : 'Confirmar Agendamento'}
                </button>
              </form>
            </div>
          </div>

          {/* Meus Agendamentos */}
          <div>
            <div className="p-6">
              <h3 className="text-lg font-semibold text-white mb-4">Meus Agendamentos</h3>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-300 mb-2">
                  Filtrar por Status
                </label>
                <div className="flex flex-wrap gap-2">
                  <button
                    type="button"
                    onClick={() => setFiltroStatus('todos')}
                    className={`px-3 py-1 text-sm rounded-full transition-colors ${
                      filtroStatus === 'todos'
                        ? 'bg-primary-600 text-white'
                        : 'bg-slate-700 text-gray-300 hover:bg-slate-600'
                    }`}
                  >
                    Todos
                  </button>
                  <button
                    type="button"
                    onClick={() => setFiltroStatus('AGENDADO')}
                    className={`px-3 py-1 text-sm rounded-full transition-colors ${
                      filtroStatus === 'AGENDADO'
                        ? 'bg-blue-600 text-white'
                        : 'bg-slate-700 text-gray-300 hover:bg-slate-600'
                    }`}
                  >
                    Agendado
                  </button>
                  <button
                    type="button"
                    onClick={() => setFiltroStatus('CONFIRMADO')}
                    className={`px-3 py-1 text-sm rounded-full transition-colors ${
                      filtroStatus === 'CONFIRMADO'
                        ? 'bg-green-600 text-white'
                        : 'bg-slate-700 text-gray-300 hover:bg-slate-600'
                    }`}
                  >
                    Confirmado
                  </button>
                  <button
                    type="button"
                    onClick={() => setFiltroStatus('CANCELADO')}
                    className={`px-3 py-1 text-sm rounded-full transition-colors ${
                      filtroStatus === 'CANCELADO'
                        ? 'bg-red-600 text-white'
                        : 'bg-slate-700 text-gray-300 hover:bg-slate-600'
                    }`}
                  >
                    Cancelado
                  </button>
                  <button
                    type="button"
                    onClick={() => setFiltroStatus('CONCLUIDO')}
                    className={`px-3 py-1 text-sm rounded-full transition-colors ${
                      filtroStatus === 'CONCLUIDO'
                        ? 'bg-purple-600 text-white'
                        : 'bg-slate-700 text-gray-300 hover:bg-slate-600'
                    }`}
                  >
                    Concluído
                  </button>
                </div>
              </div>
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-100">
                    <tr>
                      <th className="px-4 py-2 text-left text-xs font-medium text-gray-700 uppercase">Serviço</th>
                      <th className="px-4 py-2 text-left text-xs font-medium text-gray-700 uppercase">Profissional</th>
                      <th className="px-4 py-2 text-left text-xs font-medium text-gray-700 uppercase">Data/Hora</th>
                      <th className="px-4 py-2 text-left text-xs font-medium text-gray-700 uppercase">Status</th>
                      <th className="px-4 py-2 text-left text-xs font-medium text-gray-700 uppercase">Cancelado por</th>
                      <th className="px-4 py-2 text-right text-xs font-medium text-gray-700 uppercase">Ações</th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {(() => {
                      const agendamentosFiltrados = filtroStatus === 'todos' 
                        ? agendamentos 
                        : agendamentos.filter(agendamento => agendamento.status === filtroStatus);
                      
                      return agendamentosFiltrados.length > 0 ? (
                        agendamentosFiltrados.map((agendamento, index) => (
                        <tr key={agendamento.id} className={index % 2 === 0 ? 'bg-white hover:bg-blue-300' : 'bg-gray-200 hover:bg-blue-400'}>
                          <td className="px-4 py-2 text-sm text-gray-900">{agendamento.servico?.nome}</td>
                          <td className="px-4 py-2 text-sm text-gray-600">{agendamento.profissional?.nome}</td>
                          <td className="px-4 py-2 text-sm text-gray-500">{formatarData(agendamento.dataAgendamento)} às {agendamento.horarioAgendado}</td>
                          <td className="px-4 py-2 text-sm">
                            <span className={`inline-block px-2 py-1 text-xs rounded ${
                              agendamento.status === 'AGENDADO' ? 'bg-blue-100 text-blue-800' :
                              agendamento.status === 'CONFIRMADO' ? 'bg-green-100 text-green-800' :
                              agendamento.status === 'CANCELADO' ? 'bg-red-100 text-red-800' :
                              'bg-gray-100 text-gray-800'
                            }`}>
                              {agendamento.status}
                            </span>
                          </td>
                          <td className="px-4 py-2 text-sm text-gray-600">
                            {agendamento.canceledByUserName || '-'}
                          </td>
                          <td className="px-4 py-2 text-right text-sm">
                            {user?.role === 'ADMIN' && agendamento.status === 'AGENDADO' && (
                              <button
                                onClick={() => handleConfirm(agendamento.id)}
                                className="p-1 rounded transition-all text-green-600 hover:text-green-900 hover:bg-green-50 mr-1"
                                title="Confirmar"
                              >
                                <Check className="h-4 w-4" />
                              </button>
                            )}
                            <button
                              onClick={() => handleEdit(agendamento)}
                              disabled={agendamento.status === 'CANCELADO'}
                              className={`p-1 rounded transition-all ${
                                agendamento.status === 'CANCELADO'
                                  ? 'text-gray-400 cursor-not-allowed'
                                  : 'text-blue-600 hover:text-blue-900 hover:bg-blue-50'
                              }`}
                              title="Editar"
                            >
                              <Edit2 className="h-4 w-4" />
                            </button>
                            <button
                              onClick={() => handleCancel(agendamento.id)}
                              disabled={agendamento.status === 'CANCELADO'}
                              className={`p-1 rounded transition-all ${
                                agendamento.status === 'CANCELADO'
                                  ? 'text-gray-400 cursor-not-allowed'
                                  : 'text-red-600 hover:text-red-900 hover:bg-red-50'
                              }`}
                              title="Cancelar"
                            >
                              <Trash2 className="h-4 w-4" />
                            </button>
                          </td>
                        </tr>
                      ))
                    ) : (
                      <tr>
                        <td colSpan="5" className="px-4 py-4 text-center text-gray-400">
                          Nenhum agendamento encontrado
                        </td>
                      </tr>
                    );
                    })()}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </main>

      {/* Modal de Confirmação de Cancelamento */}
      {showCancelConfirm && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div 
            className="rounded-lg p-6 max-w-md w-full mx-4"
            style={{
              background: 'rgba(30, 41, 59, 0.95)',
              border: '1px solid rgba(71, 85, 105, 0.5)'
            }}
          >
            <h3 className="text-lg font-semibold text-white mb-4">
              Confirmar Cancelamento
            </h3>
            <p className="text-gray-300 mb-6">
              Tem certeza que deseja cancelar este agendamento?
            </p>
            <div className="flex justify-end space-x-3">
              <button
                onClick={() => {
                  setShowCancelConfirm(false);
                  setCancelingId(null);
                }}
                className="px-4 py-2 bg-slate-700 text-gray-300 rounded hover:bg-slate-600 transition-colors"
              >
                Não
              </button>
              <button
                onClick={confirmCancel}
                className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 transition-colors"
              >
                Sim, Cancelar
              </button>
            </div>
          </div>
        </div>
      )}
      </div>
    </div>
  );
};

export default Agendamento;
