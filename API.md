# 🛠️ API Docs - FurWatch

## 📋 Sumário

* [Autenticação](#-autenticação)
* [Banimento de Itens](#-banimento-de-itens)

  * [GET /banitem](#get-banitem)
  * [POST /banitem (banir)](#post-banitem-banir)
  * [POST /banitem (desbanir)](#post-banitem-desbanir)
* [Verificação de IP e Staff](#-verificação-de-ip-e-staff)

  * [GET /verify](#get-verify)
  * [POST /verify](#post-verify)
* [Rastreamento de IP de Jogadores](#-rastreamento-de-ip-de-jogadores)

  * [POST /p_ip](#post-p_ip)

---

## 🔐 Autenticação

Todas as requisições exigem **autenticação via Bearer Token**.

**Header obrigatório:**

```http
Authorization: Bearer API_KEY
```

Se a `API_KEY` for inválida ou ausente, o servidor deve retornar:

```json
{
  "error": "Unauthorized"
}
```

**HTTP Status:** `401 Unauthorized`

---

## 🕵️‍ Segurança

### **POST /security**

Envia dados de segurança para anticheat e moderação.

**Endpoint:**

```
POST /security
```

**Headers:**

```http
Authorization: Bearer API_KEY
```

**Body:**

```json
{
  "modFileHashes": {
    "fursmp-1.0.4.jar": "e241221e315a407b82c6e5931b2266545121578c5d37690a27daaebf0bf91c8a"
  },
  "modlist": [ "minecraft@1.21.1", "fursmp@1.0.4", "neoforge@21.1.193" ],
  "ip_address": "::1",
  "username": "FNPC13",
  "timestamp": 1762209005011
}
```

**Body Params:**

| Parâmetro | Tipo     | Descrição                                           |
| --------  | -------- | --------------------------------------------------- |
| `username`  | `string` | `nickname do jogador que executou a ação.` |
| `ip_address`  | `string` | `endereço ip da conexão do jogador.`            |
| `modlist`  | `list<string>` | `lista de mods carregados no client (modId@versão)`            |
| `modFileHashes`  | `map<string, string>` | `o nome e o hash de cada arquivo dentro da pasta mods do jogador.`            |

**HTTP Status:** `200 OK`

---

## 🚫 Banimento de Itens

### **GET /banitem**

Obtém a lista atual de itens banidos.

**Endpoint:**

```
GET /banitem?timestamp=
```

**Headers:**

```http
Authorization: Bearer API_KEY
```

**Query Params:**

| Parâmetro   | Tipo     | Descrição                                                  |
| ----------- | -------- | ---------------------------------------------------------- |
| `timestamp` | `string` | Opcional. Usado para evitar cache ou sincronizar com logs. |

**Exemplo de resposta:**

```json
["minecraft:bedrock", "minecraft:dirt"]
```

**HTTP Status:** `200 OK`

---

### **POST /banitem**

Adiciona ou remove um item à lista de banidos.

**Endpoint:**

```
POST /banitem
```

**Headers:**

```http
Authorization: Bearer API_KEY
```

**Body:**

```json
{
  "item": "minecraft:barrier",
  "staff": "FURSMP4",
  "action": "add",
  "reason": "Invisible block?"
}
```

**Body Params:**

| Parâmetro | Tipo     | Descrição                                           |
| --------  | -------- | --------------------------------------------------- |
| `action`  | `string` | `add ou remove, especifica a ação sob o item atual` |
| `reason`  | `string` | `(opcional) motivo do banimento do item`            |

**HTTP Status:** `200 OK`

---

### **POST /banitem (desbanir)**

Remove um item da lista de banidos.

**Endpoint:**

```
POST /banitem
```

**Headers:**

```http
Authorization: Bearer API_KEY
```

**Body:**

```json
{
  "item": "minecraft:barrier",
  "staff": "FURSMP4",
  "action": "remove"
}
```

**HTTP Status:** `200 OK`

---

## 🧠 Verificação de IP e Staff

### **GET /verify**

Verifica se um usuário é staff **e** se o IP está autorizado para ele.

**Endpoint:**

```
GET /verify?username=FURSMP4&ip=127.0.0.1&timestamp=
```

**Headers:**

```http
Authorization: Bearer API_KEY
```

**Query Params:**

| Parâmetro   | Tipo     | Descrição                                   |
| ----------- | -------- | ------------------------------------------- |
| `username`  | `string` | Nome do jogador/staff                       |
| `ip`        | `string` | Endereço IP atual do jogador                |
| `timestamp` | `string` | Opcional — usado para sincronização de logs |

**Respostas possíveis:**

✅ **Se o usuário for staff e o IP estiver autorizado:**

```text
OK
```

❌ **Se o usuário for staff mas o IP não for autorizado:**

```text
❌ IP não autorizado! A staff foi avisada!
```

**HTTP Status:** `200 OK`

---

### **POST /verify**

Cria uma nova verificação de IP e notifica a staff.

**Endpoint:**

```
POST /verify
```

**Headers:**

```http
Authorization: Bearer API_KEY
```

**Body:**

```json
{
  "username": "FURSMP4",
  "ip": "127.0.0.1",
  "timestamp": 1730065452
}
```

**HTTP Status:** `200 OK`

---

## 📡 Rastreamento de IP de Jogadores

### **POST /p_ip**

Registra o IP do jogador a cada login (para auditoria ou segurança).

**Endpoint:**

```
POST /p_ip
```

**Headers:**

```http
Authorization: Bearer API_KEY
```

**Body:**

```json
{
  "username": "FURSMP4",
  "ip": "127.0.0.1",
  "timestamp": 1730065452
}
```

**HTTP Status:** `200 OK`

---

## 🧾 Códigos de Status

| Código | Significado                            |
| ------ | -------------------------------------- |
| 200    | Operação concluída com sucesso         |
| 400    | Requisição malformada                  |
| 401    | Falha de autenticação (token inválido) |
| 403    | Permissão insuficiente                 |
| 404    | Endpoint não encontrado                |
| 500    | Erro interno do servidor               |

---