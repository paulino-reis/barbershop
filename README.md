# SmartSalão - Sistema de Agendamento Multi-Tenant para Barbearias

Um sistema completo de agendamento para barbearias com suporte a multi-tenancy, desenvolvido com Java 21, Spring Boot 4.0.5 e React.

## Visão Geral

O SmartSalão é uma plataforma SaaS que permite que múltiplas barbearias utilizem o mesmo sistema através de subdomínios personalizados (ex: `talison.smartsalao.com.br`). Cada tenant (barbearia) tem seus dados isolados através de um sistema de multi-tenancy baseado em tenant_id.

## Tecnologias Utilizadas

### Backend
- **Java 21** - Linguagem de programação com Virtual Threads habilitadas
- **Spring Boot 4.0.5** - Framework principal
- **Spring Security** - Autenticação e autorização com JWT
- **Spring Data JPA** - Persistência de dados com Hibernate 7.2.7
- **MySQL 8.0** - Banco de dados relacional
- **HikariCP** - Pool de conexões JDBC
- **Lombok** - Redução de código boilerplate
- **AspectJ** - Programação orientada a aspectos para filtros de tenant
- **Maven** - Gerenciamento de dependências

### Frontend
- **React 18** - Biblioteca JavaScript
- **React Router** - Navegação
- **TailwindCSS** - Estilização
- **Lucide React** - Ícones
- **Axios** - Cliente HTTP
- **Chart.js** - Gráficos de estatísticas
- **react-chartjs-2** - Integração do Chart.js com React

## Arquitetura Multi-Tenant

O sistema utiliza um padrão de multi-tenancy baseado em **tenant_id** para isolar os dados de cada barbearia:

### Componentes do Sistema Multi-Tenant

1. **TenantFilter** (`com.hair.service.TenantFilter`)
   - Intercepta todas as requisições HTTP
   - Extrai o slug do tenant do subdomínio (ex: `talison` de `talison.smartsalao.com.br`)
   - Busca o `tenant_id` correspondente na tabela `tenant_config`
   - Define o contexto do tenant via `TenantContext`

2. **TenantContext** (`com.hair.service.TenantContext`)
   - Armazena o `tenant_id` em um `ThreadLocal`
   - Permite acesso ao tenant atual durante o processamento da requisição

3. **TenantEntityListener** (`com.hair.service.TenantEntityListener`)
   - Listener JPA `@PrePersist`
   - Define automaticamente o `tenant_id` em entidades que estendem `BaseEntity`
   - Garante que todo registro seja associado ao tenant correto

4. **TenantAspect** (`com.hair.service.TenantAspect`)
   - Aspecto AOP que habilita o filtro Hibernate de tenant
   - Aplica o filtro antes de executar qualquer operação de serviço
   - Garante que queries retornem apenas dados do tenant atual

5. **BaseEntity** (`com.hair.model.BaseEntity`)
   - Classe abstrata com campo `tenant_id`
   - Configura filtro Hibernate para isolamento de dados
   - Todas as entidades tenant-aware estendem esta classe

### Tabela de Configuração de Tenants

A tabela `tenant_config` armazena:
- `id` - Identificador único do tenant
- `slug` - Slug usado no subdomínio (ex: `talison`)
- `nome` - Nome da barbearia
- `tenant_id` - ID usado para isolamento de dados

## Funcionalidades

### Autenticação e Segurança
- Login e registro de usuários
- Criptografia de senhas com BCrypt
- Tokens JWT para autenticação stateless
- Proteção de rotas com Spring Security
- Redirecionamento automático para login em requisições não autenticadas

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
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/barbearia?useSSL=false&serverTimezone=America/Sao_Paulo&allowPublicKeyRetrieval=true
spring.datasource.username=${DB_USERNAME:barber_user}
spring.datasource.password=${DB_PASSWORD:sua_senha}
```

**Nota:** O timezone está configurado como `America/Sao_Paulo` para evitar problemas com datas incorretas no agendamento.

## Como Executar

### Backend
1. Navegue até a raiz do projeto
2. Execute com Maven:
```bash
mvn spring-boot:run
```
O backend estará disponível em `http://localhost:8090`

**Configurações:**
- Porta: 8090 (configurável em `application.properties`)
- Logs: Escritos em `logs/application.log`
- Virtual Threads: Habilitadas para melhor performance

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
- `GET /api/agendamentos/horarios-ocupados` - Horários ocupados
- `POST /api/agendamentos/{id}/cancelar` - Cancelar agendamento
- `POST /api/agendamentos/{id}/confirmar` - Confirmar agendamento (envia WhatsApp)
- `GET /api/agendamentos/estatisticas` - Estatísticas de serviços por profissional
- `GET /api/agendamentos/por-data` - Buscar agendamentos por data específica (admin)

### Tenant Configuration
- `GET /api/v1/config/tenant` - Obter configuração do tenant atual
- `POST /api/v1/config/tenant` - Criar configuração de novo tenant

### Usuários
- `GET /api/usuarios/perfil` - Perfil do usuário
- `PUT /api/usuarios/{id}` - Atualizar perfil
- `POST /api/usuarios/alterar-senha` - Alterar senha

## Funcionalidades Implementadas

- [x] Sistema de autenticação com JWT
- [x] Sistema multi-tenant com isolamento de dados por tenant_id
- [x] Cadastro de profissionais
- [x] Cadastro de serviços
- [x] Sistema de agendamento com calendário
- [x] Horários disponíveis (9h-21h)
- [x] Interface responsiva web e mobile
- [x] Criptografia de senhas com BCrypt
- [x] Filtros por profissional
- [x] Histórico de agendamentos
- [x] Cancelamento de agendamentos
- [x] Confirmação de agendamentos com envio de WhatsApp
- [x] Mensagem formatada com dados do agendamento
- [x] Gráfico de estatísticas de serviços por profissional
- [x] Filtro de estatísticas por período e mês
- [x] Efeitos de hover aprimorados na interface
- [x] Painel de agendamentos do dia com visualização em grade
- [x] Auto-refresh de agendamentos a cada 30 segundos
- [x] Logging em arquivo para debug
- [x] Configuração de timezone correta (America/Sao_Paulo)

## Próximos Passos

- [ ] Sistema de notificações por email
- [ ] Integração com pagamento online
- [ ] Sistema de avaliação de serviços
- [ ] Upload de fotos dos profissionais
- [ ] Recuperação de senha por email

## Licença

Este projeto está licenciado sob a MIT License.
