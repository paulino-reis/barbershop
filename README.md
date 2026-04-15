# Sistema de Barbearia - Hair Management System

Um sistema completo de agendamento para barbearias desenvolvido com Java 21, Spring Boot 3.x e React.

## Tecnologias Utilizadas

### Backend
- **Java 21** - Linguagem de programação
- **Spring Boot 3.x** - Framework principal
- **Spring Security** - Autenticação e autorização
- **Spring Data JPA** - Persistência de dados
- **JWT** - Tokens de autenticação
- **MySQL** - Banco de dados
- **Maven** - Gerenciamento de dependências

### Frontend
- **React 18** - Biblioteca JavaScript
- **React Router** - Navegação
- **TailwindCSS** - Estilização
- **Lucide React** - Ícones
- **Axios** - Cliente HTTP
- **Chart.js** - Gráficos de estatísticas
- **react-chartjs-2** - Integração do Chart.js com React

## Funcionalidades

### Autenticação e Segurança
- Login e registro de usuários
- Criptografia de senhas com BCrypt
- Tokens JWT para autenticação
- Proteção de rotas

### Cadastros
- **Profissionais**: nome, telefone, foto, data de início, endereço completo
- **Serviços**: ID, preço, data de última alteração
- **Agendamentos**: profissional, serviço, data, horário
- **Usuários**: nome, telefone, login, senha, histórico

### Sistema de Agendamento
- Calendário interativo
- Seleção de profissionais ou "todos"
- Horários disponíveis (9h às 21h)
- Filtros por profissional
- Status de agendamento (agendado, confirmado, cancelado, concluído)
- Confirmação automática com envio de mensagem via WhatsApp
- Mensagem formatada com dados do agendamento (cliente, serviço, data, horário, profissional)
- Texto em negrito nos campos importantes da mensagem WhatsApp

### Interface Responsiva
- Design mobile-first
- Layout adaptável para desktop e mobile
- Interface moderna e intuitiva
- Efeitos de hover aprimorados nos menus de navegação e abas

### Estatísticas e Relatórios
- Gráfico de pizza mostrando serviços por profissional
- Filtro por período específico (intervalo de datas)
- Filtro por mês específico
- Exibição de quantidade de serviços no gráfico
- Legenda interativa com efeitos de hover
- Modal informativo quando não há dados disponíveis
- Exclusão de agendamentos cancelados das estatísticas

## Estrutura do Projeto

```
hair/
|-- src/main/java/com/hair/
|   |-- controller/          # REST APIs
|   |-- model/              # Entidades JPA
|   |-- repository/         # Spring Data JPA
|   |-- security/           # Configuração de segurança
|   |-- service/            # Lógica de negócio
|   |-- HairApplication.java
|-- src/main/resources/
|   |-- application.properties
|-- frontend/
|   |-- public/
|   |-- src/
|   |   |-- components/     # Componentes React
|   |   |-- contexts/       # Contextos React
|   |   |-- pages/          # Páginas da aplicação
|   |   |-- App.js
|   |   |-- index.js
|   |   |-- index.css
|   |-- package.json
|   |-- tailwind.config.js
```

## Configuração do Banco de Dados

1. Crie o banco de dados MySQL:
```sql
CREATE DATABASE barbearia;
```

2. Configure as credenciais em `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/barbearia?useSSL=false&serverTimezone=UTC
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
```

## Como Executar

### Backend
1. Navegue até a raiz do projeto
2. Execute com Maven:
```bash
mvn spring-boot:run
```
O backend estará disponível em `http://localhost:8080`

### Frontend
1. Navegue até a pasta `frontend`:
```bash
cd frontend
```

2. Instale as dependências:
```bash
npm install
```

3. Inicie o servidor de desenvolvimento:
```bash
npm start
```
O frontend estará disponível em `http://localhost:3000`

## APIs Principais

### Autenticação
- `POST /api/auth/login` - Login de usuário
- `POST /api/auth/registrar` - Registro de novo usuário

### Profissionais
- `GET /api/profissionais` - Listar todos
- `POST /api/profissionais` - Criar novo
- `PUT /api/profissionais/{id}` - Atualizar
- `DELETE /api/profissionais/{id}` - Deletar

### Serviços
- `GET /api/servicos` - Listar todos
- `POST /api/servicos` - Criar novo
- `PUT /api/servicos/{id}` - Atualizar
- `DELETE /api/servicos/{id}` - Deletar

### Agendamentos
- `GET /api/agendamentos` - Listar agendamentos do usuário
- `POST /api/agendamentos` - Criar novo agendamento
- `GET /api/agendamentos/horarios-disponiveis` - Horários disponíveis
- `POST /api/agendamentos/{id}/cancelar` - Cancelar agendamento
- `POST /api/agendamentos/{id}/confirmar` - Confirmar agendamento (envia WhatsApp)
- `GET /api/agendamentos/estatisticas` - Estatísticas de serviços por profissional

### Usuários
- `GET /api/usuarios/perfil` - Perfil do usuário
- `PUT /api/usuarios/{id}` - Atualizar perfil
- `POST /api/usuarios/alterar-senha` - Alterar senha

## Funcionalidades Implementadas

- [x] Sistema de autenticação com JWT
- [x] Cadastro de profissionais
- [x] Cadastro de serviços
- [x] Sistema de agendamento com calendário
- [x] Horários disponíveis (9h-21h)
- [x] Interface responsiva web e mobile
- [x] Criptografia de senhas
- [x] Filtros por profissional
- [x] Histórico de agendamentos
- [x] Cancelamento de agendamentos
- [x] Confirmação de agendamentos com envio de WhatsApp
- [x] Mensagem formatada com dados do agendamento
- [x] Gráfico de estatísticas de serviços por profissional
- [x] Filtro de estatísticas por período e mês
- [x] Efeitos de hover aprimorados na interface

## Próximos Passos

- [ ] Sistema de notificações por email
- [ ] Integração com pagamento online
- [ ] Sistema de avaliação de serviços
- [ ] Upload de fotos dos profissionais
- [ ] Recuperação de senha por email

## Licença

Este projeto está licenciado sob a MIT License.
