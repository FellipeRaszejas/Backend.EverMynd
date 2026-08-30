# EverMynd

Plataforma de saúde mental que conecta pacientes a psicólogos/médicos, com consultas (online, presencial ou VR), assinaturas pagas e um painel administrativo de verificação de profissionais.

Backend em **Spring Boot 4** (Java 21) + **MySQL** + **Flyway**, com frontend em **React (Vite)** consumindo a API via JWT.

> ⚠️ Projeto em desenvolvimento/prototipação. Várias integrações (Mercado Pago, Google OAuth, papel de admin) estão com credenciais placeholder e pendências documentadas abaixo.

---

## Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4.1.0 |
| Persistência | Spring Data JPA + Hibernate 7 |
| Banco de dados | MySQL 8 |
| Migrations | Flyway (`spring-boot-starter-flyway`) |
| Autenticação | JWT (`jjwt`) + Spring Security |
| Pagamentos | Mercado Pago (Payments API, cobrança direta síncrona) |
| Login social | Google OAuth (ID Token verification) |
| Build | Maven |
| Frontend | React + Vite, React Router |

---

## Arquitetura

Fluxo padrão de uma requisição:

```
Cliente HTTP
   │
   ▼
Segurança (JwtAuthenticationFilter + SecurityConfig)
   │
   ▼
Controllers (REST API)
   │
   ▼
Services (regras de negócio, validações, orquestração)
   │
   ▼
Repositories (Spring Data JPA)
   │
   ▼
MySQL
```

Cross-cutting:
- **`GlobalExceptionHandler`** — traduz exceptions de qualquer service em respostas HTTP com status correto.
- **`CurrentUserProvider`** — resolve o usuário autenticado a partir do `SecurityContext` (usado dentro dos services, não dos controllers).

### Domínios (pacotes)

| Pacote | Responsabilidade |
|---|---|
| `user` | Cadastro, login (email/senha e Google), entidade `User`, roles (`PATIENT`, `DOCTOR`, `ADMIN`) |
| `patient` | Perfil de paciente (`PatientProfile`, 1:1 com `User`) |
| `doctor` | Perfil de médico (`DoctorProfile`), fluxo de verificação (`PENDING`/`APPROVED`/`REJECTED`) |
| `appointment` | Consultas entre paciente e médico, com checagem de conflito de horário |
| `subscription` | Planos pagos (mensal/anual), integração com Mercado Pago |
| `admin` | Endpoints restritos a `ROLE_ADMIN` (aprovação de médicos) |
| `security` | JWT, filtro de autenticação, configuração do Spring Security |
| `common` | Tratamento global de exceções |
| `health` | Health check (`/api/health`) |
| `config` | Beans de infraestrutura (ex: `RestTemplate`) |

---

## Modelo de dados

5 tabelas, todas com `id` do tipo `UUID` (armazenado como `CHAR(36)` no MySQL):

- **`users`** — dados de conta (email, hash de senha, `google_id`, role, status).
- **`doctor_profiles`** — 1:1 com `users` (`@MapsId`), especialidade, bio, faixa de preço, status de verificação.
- **`patient_profiles`** — 1:1 com `users`, data de nascimento, modo anônimo.
- **`appointments`** — consulta entre `doctor_profiles` e `patient_profiles`, tipo (`ONLINE`/`IN_PERSON`/`VR`), status, horários.
- **`subscriptions`** — assinatura do usuário, plano, status, vigência, `payment_id` do Mercado Pago.

Schema versionado via Flyway em `src/main/resources/db/migration/V1__init.sql`.

---

## Autenticação

- Login tradicional: `email` + `password` (BCrypt), gera JWT com `sub` (userId), `email`, `role`.
- Login social: `POST /api/v1/auth/google` recebe um `idToken` do Google Sign-In, valida contra `google.client-id`, cria conta automaticamente como `PATIENT` no primeiro acesso (ou vincula a uma conta existente pelo email).
- Token válido por `jwt.expiration-ms` (1h por padrão). **Não há refresh token implementado ainda** — expirado, o usuário precisa logar de novo.
- Rotas públicas: `/api/health`, `/api/v1/auth/**`, `GET /api/v1/doctors` (e `/{id}`), `/swagger-ui/**`, `/v3/api-docs/**`.
- `/api/v1/admin/**` exige `ROLE_ADMIN` — **hoje não existe nenhum fluxo para criar um admin**; precisa ser feito manualmente via SQL (`UPDATE users SET role='ADMIN' WHERE email='...'`).

---

## Regras de negócio já implementadas

- **Conflito de agenda**: um médico não pode ter duas consultas não-canceladas sobrepostas no mesmo horário.
- **Assinatura única ativa**: um usuário não pode ter duas assinaturas `ACTIVE` simultâneas.
- **Expiração automática**: job agendado (`@Scheduled`, diário às 01:00) marca assinaturas vencidas como `EXPIRED`.
- **Verificação de médico**: todo médico nasce com `verificationStatus = PENDING`; só aparece na busca pública (`GET /api/v1/doctors`) depois de um admin aprovar via `PATCH /api/v1/admin/doctors/{id}/verification`.
- **Pagamento síncrono**: `POST /api/v1/subscriptions` só persiste a assinatura no banco **depois** de receber aprovação do Mercado Pago — se o cartão for recusado, nada é gravado.

---

## Configuração (`application.properties`)

```properties
# JWT
jwt.secret=<string aleatória de 32+ caracteres>
jwt.expiration-ms=3600000

# Google OAuth
google.client-id=<client id do Google Cloud Console>

# Mercado Pago
mercadopago.access-token=<access token de teste ou produção>
mercadopago.base-url=https://api.mercadopago.com

# Preços das assinaturas (BRL)
subscription.price.monthly=29.90
subscription.price.annual=299.00

# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/evermynd?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=<sua senha>
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=false
spring.jpa.properties.hibernate.type.preferred_uuid_jdbc_type=CHAR

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
```

> O encoding do arquivo precisa ser **UTF-8** — o projeto está configurado para não filtrar/interpretar `src/main/resources` durante o build (`<filtering>false</filtering>` no `pom.xml`), justamente para evitar erros de encoding na cópia dos arquivos de configuração.

---

## Como rodar localmente

### 1. Banco de dados
Crie um banco MySQL vazio chamado `evermynd`:
```sql
CREATE DATABASE evermynd;
```
As tabelas são criadas automaticamente pelo Flyway na primeira subida da aplicação — não crie tabelas manualmente.

### 2. Backend
```bash
mvn clean spring-boot:run
```
(No Eclipse: **Run As → Maven Build...** com goals `clean spring-boot:run` — mais confiável que o botão "Run" padrão, que às vezes não sincroniza os recursos de `src/main/resources`.)

Log de sucesso esperado: Flyway migrando o schema, seguido de `Started EverMyndApplication`.

### 3. Frontend
```bash
npm install
npm run dev
```
Cria um `.env` na raiz do frontend:
```
VITE_API_URL=http://localhost:8080
```
Servidor sobe por padrão em `http://localhost:5173` — origem já liberada no CORS do backend (`SecurityConfig`).

---

## Endpoints principais

| Método | Rota | Auth | Descrição |
|---|---|---|---|
| GET | `/api/health` | público | Health check |
| POST | `/api/v1/auth/register` | público | Cadastro (paciente ou médico) |
| POST | `/api/v1/auth/login` | público | Login email/senha |
| POST | `/api/v1/auth/google` | público | Login via Google |
| GET | `/api/v1/patients/me` | autenticado | Perfil do paciente logado |
| PUT | `/api/v1/patients/me` | autenticado | Atualiza perfil do paciente |
| GET | `/api/v1/doctors` | público | Lista médicos **aprovados** |
| GET | `/api/v1/doctors/{id}` | público | Detalhe de um médico |
| PUT | `/api/v1/doctors/me` | autenticado | Médico atualiza o próprio perfil |
| POST | `/api/v1/appointments` | autenticado | Paciente cria consulta |
| PATCH | `/api/v1/appointments/{id}/status` | autenticado | Médico/paciente atualiza status |
| POST | `/api/v1/subscriptions` | autenticado | Cria assinatura (cobra no Mercado Pago) |
| GET | `/api/v1/subscriptions/active` | autenticado | Assinatura ativa do usuário |
| POST | `/api/v1/subscriptions/{id}/cancel` | autenticado | Cancela assinatura |
| GET | `/api/v1/admin/doctors/pending` | `ROLE_ADMIN` | Lista médicos aguardando aprovação |
| PATCH | `/api/v1/admin/doctors/{id}/verification` | `ROLE_ADMIN` | Aprova/rejeita médico |

---

## Pendências conhecidas

- [ ] **Bug em investigação**: `GET /api/v1/patients/me` retorna `403` mesmo com token válido no header — causa ainda não identificada, log de diagnóstico adicionado em `JwtService.isTokenValid`.
- [ ] Fluxo de criação do primeiro usuário `ADMIN` (hoje só via SQL manual).
- [ ] Refresh token (sessão expira em 1h sem renovação automática).
- [ ] Testes automatizados (unitários/integração).
- [ ] Documentação OpenAPI/Swagger (dependência ainda não adicionada).
- [ ] `com.evermynd.user.service.UserService` e `com.evermynd.user.Authentication` — classes vazias, destino a definir (remover ou implementar).
- [ ] Upload de foto de perfil (frontend já tem placeholder, backend não tem endpoint).
- [ ] "Diário emocional" no dashboard do paciente é só mockado em estado local do React, sem persistência no backend.
- [ ] Deploy em produção (MySQL local via Docker por enquanto; planejado migrar para AWS).

---

## Decisões de design registradas

- **`Subscription.id` é `UUID`**, não `Long` — padronizado com o resto das entidades do projeto (originalmente estava `Long`/`IDENTITY`, foi corrigido).
- **Gateway de pagamento**: optou-se por Mercado Pago (API de cobrança direta e síncrona) em vez de InfinitePay, cujo produto público disponível é baseado em link de pagamento + webhook — modelo mais complexo e desnecessário para a fase de prototipação atual.
- **UUID armazenado como `CHAR(36)`** no MySQL (não `BINARY(16)`, que é o padrão do Hibernate 7) — trade-off deliberado para manter os IDs legíveis durante debug manual no MySQL Workbench.
