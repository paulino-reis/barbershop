import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { User, Phone, Mail, Lock, Save, Eye, EyeOff, Scissors, Calendar } from 'lucide-react';
import axios from 'axios';
import Navigation from '../components/Navigation';

const Perfil = () => {
  const { user } = useAuth();
  const [formData, setFormData] = useState({
    nomeUsuario: '',
    telefone: '',
    login: ''
  });
  const [senhaData, setSenhaData] = useState({
    senhaAtual: '',
    novaSenha: '',
    confirmarNovaSenha: ''
  });
  const [showSenhaAtual, setShowSenhaAtual] = useState(false);
  const [showNovaSenha, setShowNovaSenha] = useState(false);
  const [showConfirmarSenha, setShowConfirmarSenha] = useState(false);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [agendamentos, setAgendamentos] = useState([]);

  useEffect(() => {
    carregarPerfil();
    carregarAgendamentos();
  }, []);

  const carregarPerfil = async () => {
    try {
      const response = await axios.get('/api/usuarios/perfil');
      setFormData({
        nomeUsuario: response.data.nomeUsuario,
        telefone: response.data.telefone,
        login: response.data.login
      });
    } catch (error) {
      setMessage('Erro ao carregar perfil');
    }
  };

  const carregarAgendamentos = async () => {
    try {
      const response = await axios.get('/api/agendamentos');
      setAgendamentos(response.data);
    } catch (error) {
      console.error('Erro ao carregar agendamentos:', error);
    }
  };

  const handlePerfilChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSenhaChange = (e) => {
    setSenhaData({
      ...senhaData,
      [e.target.name]: e.target.value
    });
  };

  const handleSalvarPerfil = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage('');

    try {
      await axios.put(`/api/usuarios/${user.id}`, formData);
      setMessage('Perfil atualizado com sucesso!');
    } catch (error) {
      setMessage(error.response?.data?.message || 'Erro ao atualizar perfil');
    } finally {
      setLoading(false);
    }
  };

  const handleAlterarSenha = async (e) => {
    e.preventDefault();
    setMessage('');

    if (senhaData.novaSenha !== senhaData.confirmarNovaSenha) {
      setMessage('As senhas não coincidem');
      return;
    }

    if (senhaData.novaSenha.length < 6) {
      setMessage('A nova senha deve ter pelo menos 6 caracteres');
      return;
    }

    setLoading(true);

    try {
      await axios.post('/api/usuarios/alterar-senha', {
        senhaAtual: senhaData.senhaAtual,
        novaSenha: senhaData.novaSenha
      });
      setMessage('Senha alterada com sucesso!');
      setSenhaData({
        senhaAtual: '',
        novaSenha: '',
        confirmarNovaSenha: ''
      });
    } catch (error) {
      setMessage(error.response?.data?.message || 'Erro ao alterar senha');
    } finally {
      setLoading(false);
    }
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

  const cancelarAgendamento = async (id) => {
    if (!window.confirm('Tem certeza que deseja cancelar este agendamento?')) {
      return;
    }

    try {
      await axios.post(`/api/agendamentos/${id}/cancelar`);
      setMessage('Agendamento cancelado com sucesso!');
      carregarAgendamentos();
    } catch (error) {
      setMessage('Erro ao cancelar agendamento');
    }
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
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-white">Meu Perfil</h1>
          <p className="mt-2 text-gray-400">Gerencie suas informações e agendamentos</p>
        </div>

        {message && (
          <div className={`mb-6 p-4 rounded-lg ${
            message.includes('sucesso') 
              ? 'bg-green-900/30 text-green-300 border border-green-500/50'
              : 'bg-red-900/30 text-red-300 border border-red-500/50'
          }`}>
            {message}
          </div>
        )}

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Informações do Perfil */}
          <div 
            className="rounded-lg shadow-lg p-6"
            style={{
              background: 'rgba(255, 255, 255, 0.1)',
              backdropFilter: 'blur(10px)',
              border: '1px solid rgba(255, 255, 255, 0.2)'
            }}
          >
            <div className="flex items-center mb-6">
              <User className="h-6 w-6 text-primary-400 mr-2" />
              <h2 className="text-xl font-semibold text-white">Informações Pessoais</h2>
            </div>

            <form onSubmit={handleSalvarPerfil} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1">
                  Nome Completo
                </label>
                <input
                  type="text"
                  name="nomeUsuario"
                  value={formData.nomeUsuario}
                  onChange={handlePerfilChange}
                  className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1">
                  <Mail className="inline h-4 w-4 mr-1" />
                  Login
                </label>
                <input
                  type="text"
                  name="login"
                  value={formData.login}
                  onChange={handlePerfilChange}
                  className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1">
                  <Phone className="inline h-4 w-4 mr-1" />
                  Telefone
                </label>
                <input
                  type="tel"
                  name="telefone"
                  value={formData.telefone}
                  onChange={handlePerfilChange}
                  className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all"
                  required
                />
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full py-3 px-4 bg-gradient-to-r from-primary-600 to-primary-500 hover:from-primary-700 hover:to-primary-600 text-white font-semibold rounded-lg focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50 transition-all transform hover:scale-[1.02] disabled:transform-none shadow-lg"
              >
                <Save className="inline h-4 w-4 mr-2" />
                {loading ? 'Salvando...' : 'Salvar Alterações'}
              </button>
            </form>
          </div>

          {/* Alterar Senha */}
          <div 
            className="rounded-lg shadow-lg p-6"
            style={{
              background: 'rgba(255, 255, 255, 0.1)',
              backdropFilter: 'blur(10px)',
              border: '1px solid rgba(255, 255, 255, 0.2)'
            }}
          >
            <div className="flex items-center mb-6">
              <Lock className="h-6 w-6 text-primary-400 mr-2" />
              <h2 className="text-xl font-semibold text-white">Alterar Senha</h2>
            </div>

            <form onSubmit={handleAlterarSenha} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1">
                  Senha Atual
                </label>
                <div className="relative">
                  <input
                    type={showSenhaAtual ? 'text' : 'password'}
                    name="senhaAtual"
                    value={senhaData.senhaAtual}
                    onChange={handleSenhaChange}
                    className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all pr-12"
                    required
                  />
                  <button
                    type="button"
                    className="absolute inset-y-0 right-0 pr-3 flex items-center"
                    onClick={() => setShowSenhaAtual(!showSenhaAtual)}
                  >
                    {showSenhaAtual ? (
                      <EyeOff className="h-5 w-5 text-gray-400 hover:text-gray-200 transition-colors" />
                    ) : (
                      <Eye className="h-5 w-5 text-gray-400 hover:text-gray-200 transition-colors" />
                    )}
                  </button>
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1">
                  Nova Senha
                </label>
                <div className="relative">
                  <input
                    type={showNovaSenha ? 'text' : 'password'}
                    name="novaSenha"
                    value={senhaData.novaSenha}
                    onChange={handleSenhaChange}
                    className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all pr-12"
                    placeholder="Mínimo 6 caracteres"
                    required
                  />
                  <button
                    type="button"
                    className="absolute inset-y-0 right-0 pr-3 flex items-center"
                    onClick={() => setShowNovaSenha(!showNovaSenha)}
                  >
                    {showNovaSenha ? (
                      <EyeOff className="h-5 w-5 text-gray-400 hover:text-gray-200 transition-colors" />
                    ) : (
                      <Eye className="h-5 w-5 text-gray-400 hover:text-gray-200 transition-colors" />
                    )}
                  </button>
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1">
                  Confirmar Nova Senha
                </label>
                <div className="relative">
                  <input
                    type={showConfirmarSenha ? 'text' : 'password'}
                    name="confirmarNovaSenha"
                    value={senhaData.confirmarNovaSenha}
                    onChange={handleSenhaChange}
                    className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all pr-12"
                    placeholder="Confirme a nova senha"
                    required
                  />
                  <button
                    type="button"
                    className="absolute inset-y-0 right-0 pr-3 flex items-center"
                    onClick={() => setShowConfirmarSenha(!showConfirmarSenha)}
                  >
                    {showConfirmarSenha ? (
                      <EyeOff className="h-5 w-5 text-gray-400 hover:text-gray-200 transition-colors" />
                    ) : (
                      <Eye className="h-5 w-5 text-gray-400 hover:text-gray-200 transition-colors" />
                    )}
                  </button>
                </div>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full py-3 px-4 bg-gradient-to-r from-primary-600 to-primary-500 hover:from-primary-700 hover:to-primary-600 text-white font-semibold rounded-lg focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50 transition-all transform hover:scale-[1.02] disabled:transform-none shadow-lg"
              >
                <Lock className="inline h-4 w-4 mr-2" />
                {loading ? 'Alterando...' : 'Alterar Senha'}
              </button>
            </form>
          </div>
        </div>

        {/* Histórico de Agendamentos */}
        <div 
          className="mt-8 rounded-lg shadow-lg p-6"
          style={{
            background: 'rgba(255, 255, 255, 0.1)',
            backdropFilter: 'blur(10px)',
            border: '1px solid rgba(255, 255, 255, 0.2)'
          }}
        >
          <div className="flex items-center mb-6">
            <Calendar className="h-6 w-6 text-primary-400 mr-2" />
            <h2 className="text-xl font-semibold text-white">Meus Agendamentos</h2>
          </div>

          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-100">
                <tr>
                  <th className="px-4 py-2 text-left text-xs font-medium text-gray-700 uppercase">Serviço</th>
                  <th className="px-4 py-2 text-left text-xs font-medium text-gray-700 uppercase">Profissional</th>
                  <th className="px-4 py-2 text-left text-xs font-medium text-gray-700 uppercase">Data/Hora</th>
                  <th className="px-4 py-2 text-left text-xs font-medium text-gray-700 uppercase">Status</th>
                  <th className="px-4 py-2 text-right text-xs font-medium text-gray-700 uppercase">Ações</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {agendamentos.length > 0 ? (
                  agendamentos.map((agendamento, index) => (
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
                      <td className="px-4 py-2 text-right text-sm">
                        {agendamento.status === 'AGENDADO' && (
                          <button
                            onClick={() => cancelarAgendamento(agendamento.id)}
                            className="px-3 py-1 bg-red-600 text-white text-sm rounded hover:bg-red-700 transition-colors"
                          >
                            Cancelar
                          </button>
                        )}
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="5" className="px-4 py-4 text-center text-gray-400">
                      Nenhum agendamento encontrado
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
      </div>
    </div>
  );
};

export default Perfil;
