-- =====================================================================
-- Dados iniciais do FitWork (executado a cada start com drop-and-create)
-- Regiões: 1 Centro-Oeste | 2 Norte | 3 Nordeste | 4 Sudeste | 5 Sul
-- Tipos de proteína: 1 Concentrado | 2 Isolado | 3 Hidrolisado | 4 Blend | 5 Vegetal
-- =====================================================================

-- ---------- Estados (26 + DF) ----------
insert into estado (nome, sigla, regiao, dataCadastro) values ('Acre', 'AC', 2, now());
insert into estado (nome, sigla, regiao, dataCadastro) values ('Alagoas', 'AL', 3, now());
insert into estado (nome, sigla, regiao, dataCadastro) values ('Amapá', 'AP', 2, now());
insert into estado (nome, sigla, regiao, dataCadastro) values ('Amazonas', 'AM', 2, now());
insert into estado (nome, sigla, regiao, dataCadastro) values ('Bahia', 'BA', 3, now());
insert into estado (nome, sigla, regiao, dataCadastro) values ('Ceará', 'CE', 3, now());
insert into estado (nome, sigla, regiao, dataCadastro) values ('Distrito Federal', 'DF', 1, now());
insert into estado (nome, sigla, regiao, dataCadastro) values ('Espírito Santo', 'ES', 4, now());
insert into estado (nome, sigla, regiao, dataCadastro) values ('Goiás', 'GO', 1, now());
insert into estado (nome, sigla, regiao, dataCadastro) values ('Maranhão', 'MA', 3, now());
insert into estado (nome, sigla, regiao, dataCadastro) values ('Mato Grosso', 'MT', 1, now());
insert into estado (nome, sigla, regiao, dataCadastro) values ('Mato Grosso do Sul', 'MS', 1, now());
insert into estado (nome, sigla, regiao, dataCadastro) values ('Minas Gerais', 'MG', 4, now());
insert into estado (nome, sigla, regiao, dataCadastro) values ('Pará', 'PA', 2, now());
insert into estado (nome, sigla, regiao, dataCadastro) values ('Paraíba', 'PB', 3, now());
insert into estado (nome, sigla, regiao, dataCadastro) values ('Paraná', 'PR', 5, now());
insert into estado (nome, sigla, regiao, dataCadastro) values ('Pernambuco', 'PE', 3, now());
insert into estado (nome, sigla, regiao, dataCadastro) values ('Piauí', 'PI', 3, now());
insert into estado (nome, sigla, regiao, dataCadastro) values ('Rio de Janeiro', 'RJ', 4, now());
insert into estado (nome, sigla, regiao, dataCadastro) values ('Rio Grande do Norte', 'RN', 3, now());
insert into estado (nome, sigla, regiao, dataCadastro) values ('Rio Grande do Sul', 'RS', 5, now());
insert into estado (nome, sigla, regiao, dataCadastro) values ('Rondônia', 'RO', 2, now());
insert into estado (nome, sigla, regiao, dataCadastro) values ('Roraima', 'RR', 2, now());
insert into estado (nome, sigla, regiao, dataCadastro) values ('Santa Catarina', 'SC', 5, now());
insert into estado (nome, sigla, regiao, dataCadastro) values ('São Paulo', 'SP', 4, now());
insert into estado (nome, sigla, regiao, dataCadastro) values ('Sergipe', 'SE', 3, now());
insert into estado (nome, sigla, regiao, dataCadastro) values ('Tocantins', 'TO', 2, now());

-- ---------- Municípios (os demais são cadastrados automaticamente via ViaCEP) ----------
insert into municipio (nome, codigoIbge, id_estado, dataCadastro) values ('Palmas', '1721000', (select id from estado where sigla = 'TO'), now());
insert into municipio (nome, codigoIbge, id_estado, dataCadastro) values ('Araguaína', '1702109', (select id from estado where sigla = 'TO'), now());
insert into municipio (nome, codigoIbge, id_estado, dataCadastro) values ('Gurupi', '1709500', (select id from estado where sigla = 'TO'), now());
insert into municipio (nome, codigoIbge, id_estado, dataCadastro) values ('Goiânia', '5208707', (select id from estado where sigla = 'GO'), now());
insert into municipio (nome, codigoIbge, id_estado, dataCadastro) values ('Brasília', '5300108', (select id from estado where sigla = 'DF'), now());
insert into municipio (nome, codigoIbge, id_estado, dataCadastro) values ('São Paulo', '3550308', (select id from estado where sigla = 'SP'), now());
insert into municipio (nome, codigoIbge, id_estado, dataCadastro) values ('Rio de Janeiro', '3304557', (select id from estado where sigla = 'RJ'), now());
insert into municipio (nome, codigoIbge, id_estado, dataCadastro) values ('Belo Horizonte', '3106200', (select id from estado where sigla = 'MG'), now());
insert into municipio (nome, codigoIbge, id_estado, dataCadastro) values ('Curitiba', '4106902', (select id from estado where sigla = 'PR'), now());
insert into municipio (nome, codigoIbge, id_estado, dataCadastro) values ('Porto Alegre', '4314902', (select id from estado where sigla = 'RS'), now());

-- ---------- Categorias ----------
insert into categoria (nome, descricao, dataCadastro) values ('Whey Protein', 'Proteínas do soro do leite para recuperação e ganho de massa.', now());
insert into categoria (nome, descricao, dataCadastro) values ('Proteína Vegetal', 'Fontes proteicas 100% vegetais (ervilha, arroz, soja).', now());
insert into categoria (nome, descricao, dataCadastro) values ('Barra de Proteína', 'Snacks práticos com alto teor proteico.', now());
insert into categoria (nome, descricao, dataCadastro) values ('Pré-Treino', 'Suplementos para energia e foco antes do treino.', now());
insert into categoria (nome, descricao, dataCadastro) values ('Creatina', 'Creatina monohidratada para força e desempenho.', now());

-- ---------- Marcas ----------
insert into marca (nome, cnpj, emailContato, ativa, dataCadastro) values ('Growth Supplements', '11222333000181', 'contato@growth.com.br', true, now());
insert into marca (nome, cnpj, emailContato, ativa, dataCadastro) values ('Max Titanium', '45678912000155', 'sac@maxtitanium.com.br', true, now());
insert into marca (nome, cnpj, emailContato, ativa, dataCadastro) values ('Integralmedica', '24681357000140', 'atendimento@integralmedica.com.br', true, now());
insert into marca (nome, cnpj, emailContato, ativa, dataCadastro) values ('Dux Nutrition', '13579246000101', 'contato@duxnutrition.com', true, now());
insert into marca (nome, cnpj, emailContato, ativa, dataCadastro) values ('Probiótica', '99887766000105', 'sac@probiotica.com.br', false, now());

-- ---------- Fornecedores ----------
insert into fornecedor (razaoSocial, cnpj, telefone, email, dataCadastro) values ('Distribuidora Fit Norte LTDA', '55443322000105', '63999990001', 'compras@fitnorte.com.br', now());
insert into fornecedor (razaoSocial, cnpj, telefone, email, dataCadastro) values ('Nutri Atacado Brasil S.A.', '77123456000173', '1133330002', 'vendas@nutriatacado.com.br', now());
insert into fornecedor (razaoSocial, cnpj, telefone, email, dataCadastro) values ('Suplementos Centro-Oeste ME', '33112244000115', '62988880003', 'contato@supco.com.br', now());

-- ---------- Sabores ----------
insert into sabor (nome, descricao, dataCadastro) values ('Chocolate', 'Cacau intenso.', now());
insert into sabor (nome, descricao, dataCadastro) values ('Baunilha', 'Baunilha cremosa.', now());
insert into sabor (nome, descricao, dataCadastro) values ('Morango', 'Morango com toque de iogurte.', now());
insert into sabor (nome, descricao, dataCadastro) values ('Cookies and Cream', 'Biscoito com creme.', now());
insert into sabor (nome, descricao, dataCadastro) values ('Natural', 'Sem sabor adicionado.', now());

-- ---------- Produtos (marca, categoria e fornecedor pelos ids acima) ----------
insert into produto (nome, descricao, preco, pesoGramas, estoque, tipoProteina, ativo, id_categoria, id_marca, id_fornecedor, dataCadastro) values ('Whey Protein Concentrado 1kg', 'Whey concentrado com 24g de proteína por dose.', 129.90, 1000, 40, 1, true, 1, 1, 1, now());
insert into produto (nome, descricao, preco, pesoGramas, estoque, tipoProteina, ativo, id_categoria, id_marca, id_fornecedor, dataCadastro) values ('Whey Isolado 900g', 'Isolado de alta pureza, baixo teor de lactose.', 219.90, 900, 25, 2, true, 1, 4, 2, now());
insert into produto (nome, descricao, preco, pesoGramas, estoque, tipoProteina, ativo, id_categoria, id_marca, id_fornecedor, dataCadastro) values ('100% Whey Max 2kg', 'Blend de concentrado e isolado.', 239.00, 2000, 18, 4, true, 1, 2, 2, now());
insert into produto (nome, descricao, preco, pesoGramas, estoque, tipoProteina, ativo, id_categoria, id_marca, id_fornecedor, dataCadastro) values ('Whey Hidrolisado 1kg', 'Absorção rápida para o pós-treino.', 289.90, 1000, 8, 3, true, 1, 3, 3, now());
insert into produto (nome, descricao, preco, pesoGramas, estoque, tipoProteina, ativo, id_categoria, id_marca, id_fornecedor, dataCadastro) values ('Proteína Vegana de Ervilha 600g', 'Blend de ervilha e arroz.', 149.90, 600, 15, 5, true, 2, 1, 1, now());
insert into produto (nome, descricao, preco, pesoGramas, estoque, tipoProteina, ativo, id_categoria, id_marca, id_fornecedor, dataCadastro) values ('Barra Protein Crisp 12un', 'Caixa com 12 barras de 45g.', 99.90, 540, 30, 4, true, 3, 3, 3, now());
insert into produto (nome, descricao, preco, pesoGramas, estoque, tipoProteina, ativo, id_categoria, id_marca, id_fornecedor, dataCadastro) values ('Whey Isolado Zero Lactose 450g', 'Versão compacta do isolado.', 129.00, 450, 0, 2, false, 1, 4, 2, now());
insert into produto (nome, descricao, preco, pesoGramas, estoque, tipoProteina, ativo, id_categoria, id_marca, id_fornecedor, dataCadastro) values ('Proteína de Soja 900g', 'Isolado proteico de soja.', 119.90, 900, 12, 5, true, 2, 2, 1, now());

insert into produto_sabor (id_produto, id_sabor) values (1, 1);
insert into produto_sabor (id_produto, id_sabor) values (1, 2);
insert into produto_sabor (id_produto, id_sabor) values (1, 3);
insert into produto_sabor (id_produto, id_sabor) values (2, 1);
insert into produto_sabor (id_produto, id_sabor) values (2, 4);
insert into produto_sabor (id_produto, id_sabor) values (3, 1);
insert into produto_sabor (id_produto, id_sabor) values (3, 2);
insert into produto_sabor (id_produto, id_sabor) values (4, 5);
insert into produto_sabor (id_produto, id_sabor) values (5, 1);
insert into produto_sabor (id_produto, id_sabor) values (5, 5);
insert into produto_sabor (id_produto, id_sabor) values (6, 4);
insert into produto_sabor (id_produto, id_sabor) values (7, 2);
insert into produto_sabor (id_produto, id_sabor) values (8, 5);

-- ---------- Clientes (Endereco -> Pessoa -> Telefone -> Cliente) ----------
insert into endereco (cep, logradouro, numero, complemento, bairro, id_municipio, dataCadastro) values ('77001002', 'Quadra 101 Sul Avenida LO 1', '10', 'Sala 2', 'Plano Diretor Sul', (select id from municipio where codigoIbge = '1721000'), now());
insert into endereco (cep, logradouro, numero, complemento, bairro, id_municipio, dataCadastro) values ('01310100', 'Avenida Paulista', '1578', null, 'Bela Vista', (select id from municipio where codigoIbge = '3550308'), now());
insert into endereco (cep, logradouro, numero, complemento, bairro, id_municipio, dataCadastro) values ('74115030', 'Rua 9', '250', 'Apto 1201', 'Setor Oeste', (select id from municipio where codigoIbge = '5208707'), now());

insert into pessoa (nome, email, cpf, dataNascimento, genero, id_endereco, dataCadastro) values ('Ana Martins', 'ana.martins@fitwork.com', '52998224725', '1994-03-12', 'Feminino', 1, now());
insert into pessoa (nome, email, cpf, dataNascimento, genero, id_endereco, dataCadastro) values ('Bruno Costa', 'bruno.costa@fitwork.com', '12345678909', '1988-07-25', 'Masculino', 2, now());
insert into pessoa (nome, email, cpf, dataNascimento, genero, id_endereco, dataCadastro) values ('Carla Nunes', 'carla.nunes@fitwork.com', '11144477735', '1999-11-08', 'Feminino', 3, now());

insert into telefone (codigoArea, numero, whatsapp, id_pessoa, dataCadastro) values ('63', '999991111', true, 1, now());
insert into telefone (codigoArea, numero, whatsapp, id_pessoa, dataCadastro) values ('11', '988882222', true, 2, now());
insert into telefone (codigoArea, numero, whatsapp, id_pessoa, dataCadastro) values ('11', '33334444', false, 2, now());
insert into telefone (codigoArea, numero, whatsapp, id_pessoa, dataCadastro) values ('62', '977773333', false, 3, now());

insert into cliente (id_pessoa, aceitaNovidades, ativo, dataCadastro) values (1, true, true, now());
insert into cliente (id_pessoa, aceitaNovidades, ativo, dataCadastro) values (2, false, true, now());
insert into cliente (id_pessoa, aceitaNovidades, ativo, dataCadastro) values (3, true, true, now());
