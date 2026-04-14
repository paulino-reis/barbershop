import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { Calendar, Clock, User, Scissors, LogOut, Menu, X } from 'lucide-react';
import axios from 'axios';

const Agendamento = () => {
  const { user, logout } = useAuth();
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [selectedProfissional, setSelectedProfissional] = useState('todos');
  const [selectedServico, setSelectedServico] = useState('');
  const [selectedHorario, setSelectedHorario] = useState('');
  const [profissionais, setProfissionais] = useState([]);
  const [servicos, setServicos] = useState([]);
  const [horariosDisponiveis, setHorariosDisponiveis] = useState([]);
  const [agendamentos, setAgendamentos] = useState([]);
  const [showMobileMenu, setShowMobileMenu] = useState(false);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => {
    carregarDadosIniciais();
  }, []);

  useEffect(() => {
    if (selectedDate && selectedProfissional) {
      carregarHorariosDisponiveis();
    }
  }, [selectedDate, selectedProfissional]);

  const carregarDadosIniciais = async () => {
    try {
      const [profissionaisRes, servicosRes, agendamentosRes] = await Promise.all([
        axios.get('/api/profissionais'),
        axios.get('/api/servicos'),
        axios.get('/api/agendamentos')
      ]);

      setProfissionais(profissionaisRes.data);
      setServicos(servicosRes.data);
      setAgendamentos(agendamentosRes.data);
    } catch (error) {
      setMessage('Erro ao carregar dados');
    }
  };

  const carregarHorariosDisponiveis = async () => {
    try {
      const dataParam = selectedDate.toISOString();
      const profissionalId = selectedProfissional === 'todos' ? null : selectedProfissional;
      
      const url = profissionalId 
        ? `/api/agendamentos/horarios-disponiveis?data=${dataParam}&profissionalId=${profissionalId}`
        : `/api/agendamentos/horarios-disponiveis?data=${dataParam}`;
      
      const response = await axios.get(url);
      setHorariosDisponiveis(response.data);
    } catch (error) {
      setMessage('Erro ao carregar horários disponíveis');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!selectedServico || !selectedHorario) {
      setMessage('Selecione um serviço e um horário');
      return;
    }

    setLoading(true);
    setMessage('');

    try {
      const profissionalId = selectedProfissional === 'todos' 
        ? profissionais[0]?.id 
        : selectedProfissional;

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

      await axios.post('/api/agendamentos', agendamentoData);
      setMessage('Agendamento realizado com sucesso!');
      
      // Limpar formulário
      setSelectedServico('');
      setSelectedHorario('');
      
      // Recarregar agendamentos
      const agendamentosRes = await axios.get('/api/agendamentos');
      setAgendamentos(agendamentosRes.data);
      
    } catch (error) {
      setMessage(error.response?.data?.message || 'Erro ao realizar agendamento');
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    logout();
  };

  const formatarData = (dataString) => {
    const data = new Date(dataString);
    return data.toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center">
              <Scissors className="h-8 w-8 text-primary-600 mr-3" />
              <h1 className="text-xl font-semibold text-gray-900">Barbearia</h1>
            </div>
            
            {/* Mobile menu button */}
            <div className="md:hidden">
              <button
                onClick={() => setShowMobileMenu(!showMobileMenu)}
                className="p-2 rounded-md text-gray-400 hover:text-gray-500 hover:bg-gray-100"
              >
                {showMobileMenu ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
              </button>
            </div>

            {/* Desktop menu */}
            <div className="hidden md:flex items-center space-x-4">
              <span className="text-gray-700">Bem-vindo, {user?.nome}</span>
              <button
                onClick={handleLogout}
                className="flex items-center px-3 py-2 rounded-md text-sm font-medium text-gray-700 hover:text-gray-900 hover:bg-gray-100"
              >
                <LogOut className="h-4 w-4 mr-2" />
                Sair
              </button>
            </div>
          </div>

          {/* Mobile menu */}
          {showMobileMenu && (
            <div className="md:hidden py-4 border-t">
              <div className="flex flex-col space-y-2">
                <span className="text-gray-700 px-3 py-2">Bem-vindo, {user?.nome}</span>
                <button
                  onClick={handleLogout}
                  className="flex items-center px-3 py-2 rounded-md text-sm font-medium text-gray-700 hover:text-gray-900 hover:bg-gray-100"
                >
                  <LogOut className="h-4 w-4 mr-2" />
                  Sair
                </button>
              </div>
            </div>
          )}
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Formulário de Agendamento */}
          <div className="lg:col-span-2">
            <div className="card">
              <h2 className="text-2xl font-bold text-gray-900 mb-6">Novo Agendamento</h2>
              
              {message && (
                <div className={`mb-4 p-3 rounded ${
                  message.includes('sucesso') 
                    ? 'bg-green-50 text-green-700 border border-green-200'
                    : 'bg-red-50 text-red-700 border border-red-200'
                }`}>
                  {message}
                </div>
              )}

              <form onSubmit={handleSubmit} className="space-y-6">
                {/* Calendário */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    <Calendar className="inline h-4 w-4 mr-1" />
                    Data do Agendamento
                  </label>
                  <input
                    type="date"
                    value={selectedDate.toISOString().split('T')[0]}
                    onChange={(e) => setSelectedDate(new Date(e.target.value))}
                    min={new Date().toISOString().split('T')[0]}
                    className="input-field"
                    required
                  />
                </div>

                {/* Profissional */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    <User className="inline h-4 w-4 mr-1" />
                    Profissional
                  </label>
                  <select
                    value={selectedProfissional}
                    onChange={(e) => setSelectedProfissional(e.target.value)}
                    className="input-field"
                  >
                    <option value="todos">Todos os Profissionais</option>
                    {profissionais.map(profissional => (
                      <option key={profissional.id} value={profissional.id}>
                        {profissional.nome}
                      </option>
                    ))}
                  </select>
                </div>

                {/* Serviço */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    <Scissors className="inline h-4 w-4 mr-1" />
                    Serviço
                  </label>
                  <select
                    value={selectedServico}
                    onChange={(e) => setSelectedServico(e.target.value)}
                    className="input-field"
                    required
                  >
                    <option value="">Selecione um serviço</option>
                    {servicos.map(servico => (
                      <option key={servico.id} value={servico.id}>
                        {servico.idServico} - R$ {servico.preco.toFixed(2)}
                      </option>
                    ))}
                  </select>
                </div>

                {/* Horários Disponíveis */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    <Clock className="inline h-4 w-4 mr-1" />
                    Horários Disponíveis
                  </label>
                  <div className="grid grid-cols-3 sm:grid-cols-4 gap-2">
                    {horariosDisponiveis.length > 0 ? (
                      horariosDisponiveis.map(horario => (
                        <button
                          key={horario}
                          type="button"
                          onClick={() => setSelectedHorario(horario)}
                          className={`p-2 text-sm rounded border transition-colors ${
                            selectedHorario === horario
                              ? 'bg-primary-600 text-white border-primary-600'
                              : 'bg-white text-gray-700 border-gray-300 hover:bg-gray-50'
                          }`}
                        >
                          {horario}
                        </button>
                      ))
                    ) : (
                      <div className="col-span-full text-gray-500 text-center py-4">
                        Nenhum horário disponível para esta data
                      </div>
                    )}
                  </div>
                </div>

                <button
                  type="submit"
                  disabled={loading}
                  className="btn-primary w-full py-3"
                >
                  {loading ? 'Agendando...' : 'Confirmar Agendamento'}
                </button>
              </form>
            </div>
          </div>

          {/* Meus Agendamentos */}
          <div className="lg:col-span-1">
            <div className="card">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Meus Agendamentos</h3>
              <div className="space-y-3">
                {agendamentos.length > 0 ? (
                  agendamentos.map(agendamento => (
                    <div key={agendamento.id} className="border rounded-lg p-3">
                      <div className="font-medium text-gray-900">
                        {agendamento.servico?.idServico}
                      </div>
                      <div className="text-sm text-gray-600">
                        {agendamento.profissional?.nome}
                      </div>
                      <div className="text-sm text-gray-500">
                        {formatarData(agendamento.dataAgendamento)}
                      </div>
                      <div className="text-sm text-gray-500">
                        {agendamento.horarioAgendado}
                      </div>
                      <div className="mt-2">
                        <span className={`inline-block px-2 py-1 text-xs rounded ${
                          agendamento.status === 'AGENDADO' ? 'bg-blue-100 text-blue-800' :
                          agendamento.status === 'CONFIRMADO' ? 'bg-green-100 text-green-800' :
                          agendamento.status === 'CANCELADO' ? 'bg-red-100 text-red-800' :
                          'bg-gray-100 text-gray-800'
                        }`}>
                          {agendamento.status}
                        </span>
                      </div>
                    </div>
                  ))
                ) : (
                  <div className="text-gray-500 text-center py-4">
                    Nenhum agendamento encontrado
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default Agendamento;
