# API de Gestão de Contas a Pagar

API REST para gestão de contas a pagar, com CRUD completo, autenticação
JWT, importação assíncrona de contas via CSV (RabbitMQ) e relatório de
valor total pago por período.

---

## Arquitetura

Visão geral do fluxo de uma importação (o caso mais complexo do
projeto, por ser assíncrono):

```
Cliente
   ↓
POST /importacoes
   ↓
API
   ↓
RabbitMQ
   ↓
Consumer
   ↓
PostgreSQL
```

Internamente, cada camada só conhece a camada logo abaixo (DDD):

```
Controller
   ↓
Application Service
   ↓
Domain
   ↓
Repository
```

---

## Como executar

Pré-requisitos: Git, Docker e Docker Compose.

1. Clonar o repositório e entrar na pasta do projeto:

   ```bash
   git clone https://github.com/alexgnunes/contas-a-pagar-api.git
   cd contas-a-pagar-api
   ```

2. A partir da pasta `contas-a-pagar-api/`, criar o `.env` já com valores
   de demonstração prontos para uso local (rodar este bloco inteiro no
   terminal; funciona em Git Bash no Windows, e em qualquer shell
   Unix-like):

   ```bash
   cat > .env << 'EOF'
   DB_NAME=contas_pagar
   DB_USER=contas_pagar
   DB_PASSWORD=contas_pagar
   RABBITMQ_USER=admin
   RABBITMQ_PASSWORD=admin
   JWT_SECRET=HNeGcbOVb7PSrvAY4eC6hMuHarITPN8HZmdAZ1iZmlQ=
   ADMIN_USER=admin
   ADMIN_PASSWORD_HASH=$2a$10$gBLqo2PG9CEszx9.vCvPt.phNcRDgk6R74sPHKlujrsTxUdyzWFjC
   EOF
   ```
   O `.env` precisa ficar **na raiz de `contas-a-pagar-api/`**, a mesma
   pasta onde estão `docker-compose.yml`, `pom.xml` e `Dockerfile`. O
   arquivo tem que se chamar exatamente `.env` (não `.env.txt`, não dentro
   de subpasta). O Docker Compose procura esse nome automaticamente ali,
   sem precisar apontar caminho.

   > **Valores de avaliação/teste local: nunca usar em produção real.**
   > `ADMIN_PASSWORD_HASH` acima é o hash BCrypt de `admin123` (login:
   > `admin` / `admin123`), gerado com o mesmo `BCryptPasswordEncoder` que
   > a aplicação usa. `JWT_SECRET` é uma chave Base64 de 32 bytes gerada
   > com `openssl rand -base64 32`. O `JwtService` decodifica esse valor
   > como Base64 (`Keys.hmacShaKeyFor`), então **não pode ser um texto
   > qualquer**, precisa ser Base64 válido de pelo menos 32 bytes
   > decodificados. Ambos os valores foram gerados e testados
   > (gera + valida token, hash + `matches`) com as classes reais do
   > projeto antes de entrar aqui. Se preferir não usar valores prontos,
   > copie `.env.example` para `.env` e preencha manualmente. O
   > `docker-compose.yml` não tem defaults inseguros, então sem preencher
   > essas variáveis o `docker compose up` falha ao subir.

3. Subir a aplicação, ainda a partir de `contas-a-pagar-api/`:

   ```bash
   docker compose up
   ```

   Isso sobe PostgreSQL, RabbitMQ e a API (porta `8080`), roda as
   migrations Flyway (incluindo o seed de fornecedores e contas de
   exemplo) e deixa tudo pronto para uso.

4. Acessar o Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
   (liberado sem autenticação). Logar em `/auth/login` com `admin` /
   `admin123` (ver seção "Autenticação" abaixo) para obter o token e
   testar os demais endpoints.

   RabbitMQ Management UI: [http://localhost:15672](http://localhost:15672)
   (login com `RABBITMQ_USER`/`RABBITMQ_PASSWORD` do `.env`, ou
   `admin`/`admin` se usado o bloco pronto acima). Útil para inspecionar
   a fila de importação (`fila.importacao`) durante o processamento
   assíncrono.

---

## Autenticação

```
POST /auth/login
Content-Type: application/json
```

```json
{
  "usuario": "admin",
  "senha": "admin123"
}
```

(Credenciais válidas se o `.env` foi criado com o bloco pronto da seção
"Como executar" acima; senão, use o usuário/senha que você configurou.)

Resposta:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer"
}
```

Enviar o token nas demais chamadas: `Authorization: Bearer <token>`.

---

## Endpoints principais

### Fornecedor

```
POST /fornecedores
```
```json
{ "nome": "Fornecedor Alpha Energia" }
```

```
GET /fornecedores
GET /fornecedores/{id}
PUT /fornecedores/{id}
DELETE /fornecedores/{id}
```

Exclusão é bloqueada (`409 Conflict`) se houver alguma `Conta` vinculada
ao fornecedor.

### Conta

```
POST /contas
```
```json
{
  "descricao": "Energia - Agosto",
  "valor": 350.00,
  "dataVencimento": "2026-08-10",
  "fornecedorId": "11111111-1111-1111-1111-111111111111",
  "situacao": "PENDENTE",
  "dataPagamento": null
}
```

`situacao` é opcional (default `PENDENTE`); aceita `PAGO` ou `CANCELADO`
também, cadastrando a conta já nesse estado (útil para migração de dados
históricos). `dataPagamento` só é aceita (e é obrigatória) quando
`situacao=PAGO`, e deve ser uma data não-futura.

```
GET /contas?descricao=Energia&dataVencimentoInicial=2026-08-01&dataVencimentoFinal=2026-08-31&page=0&size=20
GET /contas/{id}
PUT /contas/{id}
```

```
PATCH /contas/{id}/situacao
```
```json
{ "situacao": "PAGO", "dataPagamento": null }
```

Marca a conta como paga ou cancelada, o que só é permitido a partir de
`PENDENTE` (invariantes de domínio, ver especificação, seção 6).
`dataPagamento` é opcional: se omitida (ou `null`), é preenchida
automaticamente com a data atual; se informada, é usada no lugar (não
pode ser futura), útil para registrar um pagamento retroativo.

```
DELETE /contas/{id}
```

### Relatório

```
GET /relatorios/total-pago?inicio=2026-08-01&fim=2026-08-31
```
```json
{
  "periodoInicio": "2026-08-01",
  "periodoFim": "2026-08-31",
  "totalPago": 15230.50
}
```

Soma só contas com `situacao = PAGO` e `dataPagamento` dentro do período.

### Importação assíncrona de CSV

```
POST /importacoes
Content-Type: multipart/form-data
arquivo: <arquivo.csv>
```
```json
{ "protocolo": "A1B2C3D4E5" }
```

Formato esperado do arquivo (colunas em ordem fixa; colunas extras no
fim, como `erro` ao reimportar um CSV de erros, são ignoradas):

```csv
descricao;valor;dataVencimento;dataPagamento;situacao;fornecedorId
Energia;350.00;2026-08-10;;PENDENTE;11111111-1111-1111-1111-111111111111
Internet;120.50;2026-08-15;2026-08-14;PAGO;22222222-2222-2222-2222-222222222222
```

Arquivos de exemplo prontos para testar pelo Swagger UI
(`docs/exemplos-csv/`), para selecionar direto no "Choose file" do
`POST /importacoes`, sem precisar montar um CSV na hora:

| Arquivo | Cenário |
|---|---|
| [`valido.csv`](docs/exemplos-csv/valido.csv) | 3 linhas válidas (PENDENTE, PAGO, CANCELADO): importação com sucesso total. |
| [`valor-e-data-invalidos.csv`](docs/exemplos-csv/valor-e-data-invalidos.csv) | 1 linha válida + 1 com valor negativo + 1 com `dataVencimento` mal formatada: importação com falha parcial (`CONCLUIDO_COM_ERROS`, 1 sucesso / 2 falhas), com CSV de erros pronto para baixar e corrigir. |
| [`sem-coluna-obrigatoria.csv`](docs/exemplos-csv/sem-coluna-obrigatoria.csv) | Falta a coluna `dataPagamento` no cabeçalho: importação inteira falha (`FALHOU`), cabeçalho fora da ordem esperada. |

Consultar o andamento pelo protocolo:

```
GET /importacoes/{protocolo}
```
```json
{
  "protocolo": "A1B2C3D4E5",
  "status": "CONCLUIDO_COM_ERROS",
  "totalRegistros": 2,
  "sucesso": 1,
  "falhas": 1,
  "downloadErros": "/importacoes/A1B2C3D4E5/erros"
}
```

Baixar o CSV só com as linhas que falharam (mesmas colunas de entrada +
coluna `erro`, pronto para corrigir e reimportar):

```
GET /importacoes/{protocolo}/erros
```

#### Fluxo assíncrono

```
Cliente
   ↓ POST /importacoes (multipart)
API
   ↓ salva o arquivo, gera protocolo, publica mensagem no RabbitMQ
RabbitMQ
   ↓
Consumer
   ↓ lê o arquivo, valida e persiste linha por linha (cada linha em sua
     própria transação, falha numa linha não desfaz as demais)
```

O RabbitMQ carrega só o protocolo na mensagem; o Consumer consulta a
importação pelo protocolo para localizar o arquivo salvo em disco.

---

## Decisões arquiteturais

Resumo das decisões mais relevantes tomadas neste projeto, com o
motivo por trás de cada uma.

- **DDD em camadas** (Controller → Application → Domain → Infrastructure),
  DTOs como Records, sem expor Entities nas APIs. Por quê: mantém as
  regras de negócio isoladas da infraestrutura, facilitando testes,
  evolução e manutenção — trocar o banco ou o broker de mensagens não
  deveria exigir tocar em regra de negócio.
- **Validação em três camadas**: Bean Validation no DTO (contrato),
  regras de fluxo no Application Service (ex: fornecedor existe),
  invariantes na própria entidade `Conta` (ex: conta paga não volta a
  pendente, valor não pode ser negativo).
- **`JOIN FETCH`** nas consultas de `Conta` que retornam `Fornecedor`,
  evitando Select N+1.

**Importação de CSV e download de erros:**

- **RabbitMQ carrega só o protocolo, nunca o CSV inteiro**: mensageria
  transporta mensagens pequenas, não arquivos. O arquivo enviado fica
  salvo em **volume Docker nomeado**, em vez do `/tmp` do container: ele
  precisa sobreviver entre o upload e o Consumer processar, o que não é
  garantido em `/tmp` se o container for recriado nesse meio-tempo.
- **Transação por linha, sem parar no primeiro erro**: cada linha é
  validada e persistida de forma independente; um CSV com 1000 linhas e
  3 erros resulta em 997 contas criadas e 3 erros registrados, nunca um
  rollback do lote inteiro.
- **Erros devolvidos como CSV pronto para reimportar, não como JSON**:
  `GET /importacoes/{protocolo}/erros` gera o CSV **só a partir do banco**
  (`ImportacaoErro` guarda a linha original no momento do erro), sem
  reabrir o arquivo enviado.
- **CSV de erros contém só as linhas inválidas**: reimportar esse
  arquivo corrige apenas o necessário, sem duplicar as linhas já
  importadas com sucesso. Risco aceito conscientemente: como as contas
  não têm um identificador natural (ex: número de fatura), reimportar o
  **arquivo original completo** duas vezes duplica os dados. Deduplicação
  por hash dos campos foi avaliada e descartada conscientemente (risco de
  falso positivo: duas contas legítimas podem ter os mesmos três campos).

**CRUD e regras de negócio:**

- **CRUD de fornecedor completo, com seed inicial via Flyway**: facilita
  testar a importação sem precisar cadastrar fornecedores manualmente
  antes.
- **Conta paga é fato histórico**: alterar `descricao`, `valor`,
  `dataVencimento` ou `fornecedor` só é permitido enquanto a conta está
  `PENDENTE`, já que mudar esses campos depois de paga comprometeria a
  rastreabilidade do histórico.
- **dataPagamento automática por padrão, mas sempre sobrescrevível**:
  no `PATCH /situacao`, se `dataPagamento` for omitida, é preenchida com
  a data atual; se informada, vale a data passada (não pode ser futura).
  Já em `POST /contas` ou na importação CSV, `dataPagamento` é sempre
  obrigatória quando `situacao=PAGO`, permitindo cadastrar a conta já
  nascendo paga com data retroativa (contas históricas ou migração de
  outro sistema).

### Melhorias futuras

- **Download do CSV original** enviado na importação.
- **Histórico de importações** (listagem, não só consulta por protocolo).
- **Cadastro de usuários com roles**: hoje é um único admin fixo via
  `.env`.
- **Refresh token** para o JWT.
- **Dead Letter Queue com retry/backoff** para mensagens do RabbitMQ que
  falham repetidamente.
- **Logs estruturados** correlacionados por protocolo/request-id
  (observabilidade, fora do escopo pedido pelo desafio).
- **Armazenamento dos arquivos de importação em S3** (ou equivalente) em
  produção, em vez do volume Docker local usado hoje.

---

## Estrutura do projeto

```
src/main/java/br/com/alexnunes/contaspagar
 ├── controller       # REST + DTOs, um pacote por recurso
 ├── application      # Services: orquestram o fluxo, chamam o domínio
 ├── domain           # Entidades, invariantes, exceptions e portas (interfaces)
 └── infrastructure   # Persistence (JPA), security (JWT), messaging (RabbitMQ), config
```

---

## 👨‍💻 Tecnologias

- ![Java](https://img.shields.io/badge/Java-17-orange)
- ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.16-brightgreen)
- ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
- ![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.13-orange)
- ![Flyway](https://img.shields.io/badge/Flyway-red)
- ![Docker Compose](https://img.shields.io/badge/Docker%20Compose-blue)
- ![Git](https://img.shields.io/badge/Git-lightgrey)

## 👨‍💻 Autor

Desenvolvido por **Alex Nunes**

🔗 [GitHub](https://github.com/alexgnunes) | [LinkedIn](https://www.linkedin.com/in/alex-gomes-nunes/)
