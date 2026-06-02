# YouTube Premium Reminder App

API REST para envio automatizado de lembretes mensais de pagamento via e-mail. Construída com Spring Boot 3, Java 21 e PostgreSQL.

## Pré-requisitos

- [Java 21+](https://adoptium.net/)
- [Maven 3.9+](https://maven.apache.org/download.cgi)
- [Docker](https://www.docker.com/products/docker-desktop/)

## Setup Local

### 1. Suba o banco de dados com Docker

```bash
docker run --name reminder-db \
  -e POSTGRES_DB=reminderdb \
  -e POSTGRES_USER=reminder \
  -e POSTGRES_PASSWORD=reminder123 \
  -p 5432:5432 \
  -d postgres:16
```

### 2. Crie as tabelas

Conecte ao banco e execute o script abaixo (via psql, DBeaver, ou outro cliente SQL):

```sql
CREATE TABLE members (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100)   NOT NULL,
    email      VARCHAR(200)   NOT NULL,
    amount     NUMERIC(10, 2) NOT NULL,
    is_active  BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE billing_records (
    id            BIGSERIAL PRIMARY KEY,
    member_id     BIGINT       NOT NULL REFERENCES members(id),
    sent_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    status        VARCHAR(10)  NOT NULL,
    error_message TEXT
);
```

Para se conectar via psql direto pelo Docker:

```bash
docker exec -it reminder-db psql -U reminder -d reminderdb
```

### 3. Configure as variáveis de ambiente

Crie um arquivo `.env` na raiz do projeto (já está no `.gitignore`):

```env
# Banco de dados
DB_URL=jdbc:postgresql://localhost:5432/reminderdb?sslmode=disable
DB_USERNAME=reminder
DB_PASSWORD=reminder123

# Gmail — use uma App Password, não a senha normal da conta
# Veja: https://myaccount.google.com/apppasswords
MAIL_USERNAME=seu-email@gmail.com
MAIL_PASSWORD=xxxx-xxxx-xxxx-xxxx

# Chave PIX que será exibida no e-mail
PIX_KEY=seu-numero-ou-chave-pix

# Cron de disparo — formato Spring: segundo minuto hora dia mês dia-semana
# Exemplo abaixo: todo dia 11 às 09h30 (horário de Brasília)
REMINDER_CRON=0 30 9 11 * *
```

> **App Password do Gmail:** acesse [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords), crie uma senha para "Outro aplicativo" e use-a em `MAIL_PASSWORD`. A verificação em duas etapas deve estar ativada na conta.

### 4. Execute a aplicação

```bash
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`.

### 5. Acesse o Swagger UI

```
http://localhost:8080/swagger-ui/index.html
```

## Endpoints principais

| Método | Rota | Descrição |
|--------|------|-----------|
| `GET` | `/api/members` | Lista todos os membros |
| `POST` | `/api/members` | Cadastra um novo membro |
| `PUT` | `/api/members/{id}` | Atualiza um membro |
| `PATCH` | `/api/members/{id}/toggle` | Ativa/desativa um membro |
| `DELETE` | `/api/members/{id}` | Remove um membro |
| `GET` | `/api/billing/history` | Histórico de cobranças |
| `POST` | `/api/billing/trigger` | Dispara o envio de e-mails manualmente |

## Lógica de disparo

O job é executado automaticamente conforme o cron definido em `REMINDER_CRON`, no fuso `America/Sao_Paulo`. Apenas membros com `is_active = true` recebem o e-mail. Cada disparo gera um registro em `billing_records` com status `SENT` ou `FAILED`.

Para testar o envio sem esperar o cron, use `POST /api/billing/trigger`.

## Parando o banco

```bash
docker stop reminder-db
docker start reminder-db   # para retomar depois
```

## Deploy

O deploy é feito automaticamente via GitHub Actions ao fazer push na branch `main`. As variáveis de ambiente são lidas dos **Secrets** do repositório no GitHub e enviadas ao DisCloud (minha escolha de plataforma para deploys backend) como arquivo `.env`.
