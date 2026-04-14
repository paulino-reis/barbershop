import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { User, Phone, Mail, Lock, Save, Eye, EyeOff, Scissors, Calendar } from 'lucide-react';
import axios from 'axios';

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
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Meu Perfil</h1>
          <p className="mt-2 text-gray-600">Gerencie suas informações e agendamentos</p>
        </div>

        {message && (
          <div className={`mb-6 p-4 rounded ${
            message.includes('sucesso') 
              ? 'bg-green-50 text-green-700 border border-green-200'
              : 'bg-red-50 text-red-700 border border-red-200'
          }`}>
            {message}
          </div>
        )}

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Informações do Perfil */}
          <div className="card">
            <div className="flex items-center mb-6">
              <User className="h-6 w-6 text-primary-600 mr-2" />
              <h2 className="text-xl font-semibold text-gray-900">Informações Pessoais</h2>
            </div>

            <form onSubmit={handleSalvarPerfil} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Nome Completo
                </label>
                <input
                  type="text"
                  name="nomeUsuario"
                  value={formData.nomeUsuario}
                  onChange={handlePerfilChange}
                  className="input-field"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  <Mail className="inline h-4 w-4 mr-1" />
                  Login
                </label>
                <input
                  type="text"
                  name="login"
                  value={formData.login}
                  onChange={handlePerfilChange}
                  className="input-field"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  <Phone className="inline h-4 w-4 mr-1" />
                  Telefone
                </label>
                <input
                  type="tel"
                  name="telefone"
                  value={formData.telefone}
                  onChange={handlePerfilChange}
                  className="input-field"
                  required
                />
              </div>

              <button
                type="submit"
                disabled={loading}
                className="btn-primary w-full py-2"
              >
                <Save className="inline h-4 w-4 mr-2" />
                {loading ? 'Salvando...' : 'Salvar Alterações'}
              </button>
            </form>
          </div>

          {/* Alterar Senha */}
          <div className="card">
            <div className="flex items-center mb-6">
              <Lock className="h-6 w-6 text-primary-600 mr-2" />
              <h2 className="text-xl font-semibold text-gray-900">Alterar Senha</h2>
            </div>

            <form onSubmit={handleAlterarSenha} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Senha Atual
                </label>
                <div className="relative">
                  <input
                    type={showSenhaAtual ? 'text' : 'password'}
                    name="senhaAtual"
                    value={senhaData.senhaAtual}
                    onChange={handleSenhaChange}
                    className="input-field pr-10"
                    required
                  />
                  <button
                    type="button"
                    className="absolute inset-y-0 right-0 pr-3 flex items-center"
                    onClick={() => setShowSenhaAtual(!showSenhaAtual)}
                  >
                    {showSenhaAtual ? (
                      <EyeOff className="h-5 w-5 text-gray-400" />
                    ) : (
                      <Eye className="h-5 w-5 text-gray-400" />
                    )}
                  </button>
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Nova Senha
                </label>
                <div className="relative">
                  <input
                    type={showNovaSenha ? 'text' : 'password'}
                    name="novaSenha"
                    value={senhaData.novaSenha}
                    onChange={handleSenhaChange}
                    className="input-field pr-10"
                    placeholder="Mínimo 6 caracteres"
                    required
                  />
                  <button
                    type="button"
                    className="absolute inset-y-0 right-0 pr-3 flex items-center"
                    onClick={() => setShowNovaSenha(!showNovaSenha)}
                  >
                    {showNovaSenha ? (
                      <EyeOff className="h-5 w-5 text-gray-400" />
                    ) : (
                      <Eye className="h-5 w-5 text-gray-400" />
                    )}
                  </button>
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Confirmar Nova Senha
                </label>
                <div className="relative">
                  <input
                    type={showConfirmarSenha ? 'text' : 'password'}
                    name="confirmarNovaSenha"
                    value={senhaData.confirmarNovaSenha}
                    onChange={handleSenhaChange}
                    className="input-field pr-10"
                    placeholder="Confirme a nova senha"
                    required
                  />
                  <button
                    type="button"
                    className="absolute inset-y-0 right-0 pr-3 flex items-center"
                    onClick={() => setShowConfirmarSenha(!showConfirmarSenha)}
                  >
                    {showConfirmarSenha ? (
                      <EyeOff className="h-5 w-5 text-gray-400" />
                    ) : (
                      <Eye className="h-5 w-5 text-gray-400" />
                    )}
                  </button>
                </div>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="btn-primary w-full py-2"
              >
                <Lock className="inline h-4 w-4 mr-2" />
                {loading ? 'Alterando...' : 'Alterar Senha'}
              </button>
            </form>
          </div>
        </div>

        {/* Histórico de Agendamentos */}
        <div className="mt-8 card">
          <div className="flex items-center mb-6">
            <Calendar className="h-6 w-6 text-primary-600 mr-2" />
            <h2 className="text-xl font-semibold text-gray-900">Meus Agendamentos</h2>
          </div>

          <div className="space-y-4">
            {agendamentos.length > 0 ? (
              agendamentos.map(agendamento => (
                <div key={agendamento.id} className="border rounded-lg p-4">
                  <div className="flex justify-between items-start">
                    <div>
                      <div className="font-medium text-gray-900 flex items-center">
                        <Scissors className="h-4 w-4 mr-2" />
                        {agendamento.servico?.idServico}
                      </div>
                      <div className="text-sm text-gray-600 mt-1">
                        Profissional: {agendamento.profissional?.nome}
                      </div>
                      <div className="text-sm text-gray-500 mt-1">
                        Data: {formatarData(agendamento.dataAgendamento)}
                      </div>
                      <div className="text-sm text-gray-500">
                        Horário: {agendamento.horarioAgendado}
                      </div>
                    </div>
                    <div className="flex flex-col items-end space-y-2">
                      <span className={`inline-block px-2 py-1 text-xs rounded ${
                        agendamento.status === 'AGENDADO' ? 'bg-blue-100 text-blue-800' :
                        agendamento.status === 'CONFIRMADO' ? 'bg-green-100 text-green-800' :
                        agendamento.status === 'CANCELADO' ? 'bg-red-100 text-red-800' :
                        'bg-gray-100 text-gray-800'
                      }`}>
                        {agendamento.status}
                      </span>
                      {agendamento.status === 'AGENDADO' && (
                        <button
                          onClick={() => cancelarAgendamento(agendamento.id)}
                          className="btn-danger text-sm py-1 px-3"
                        >
                          Cancelar
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              ))
            ) : (
              <div className="text-gray-500 text-center py-8">
                Nenhum agendamento encontrado
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Perfil;
