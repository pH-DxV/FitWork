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

-- ---------- Categorias (10) ----------
insert into categoria (nome, descricao, dataCadastro) values ('Whey Protein', 'Proteínas do soro do leite para recuperação e ganho de massa.', now());
insert into categoria (nome, descricao, dataCadastro) values ('Proteína Vegetal', 'Fontes proteicas 100% vegetais (ervilha, arroz, soja).', now());
insert into categoria (nome, descricao, dataCadastro) values ('Barra de Proteína', 'Snacks práticos com alto teor proteico.', now());
insert into categoria (nome, descricao, dataCadastro) values ('Pré-Treino', 'Suplementos para energia e foco antes do treino.', now());
insert into categoria (nome, descricao, dataCadastro) values ('Creatina', 'Creatina monohidratada para força e desempenho.', now());
insert into categoria (nome, descricao, dataCadastro) values ('Albumina', 'Proteína da clara do ovo de lenta absorção.', now());
insert into categoria (nome, descricao, dataCadastro) values ('Caseína', 'Proteína micelar de absorção gradual para uso noturno.', now());
insert into categoria (nome, descricao, dataCadastro) values ('Aminoácidos', 'BCAA, glutamina e aminoácidos essenciais.', now());
insert into categoria (nome, descricao, dataCadastro) values ('Hipercalórico', 'Fórmulas energéticas para ganho de peso e massa muscular.', now());
insert into categoria (nome, descricao, dataCadastro) values ('Colágeno', 'Peptídeos bioativos para articulações, pele e tendões.', now());

-- ---------- Marcas (10) ----------
insert into marca (nome, cnpj, emailContato, ativa, dataCadastro) values ('Growth Supplements', '11222333000181', 'contato@growth.com.br', true, now());
insert into marca (nome, cnpj, emailContato, ativa, dataCadastro) values ('Max Titanium', '45678912000155', 'sac@maxtitanium.com.br', true, now());
insert into marca (nome, cnpj, emailContato, ativa, dataCadastro) values ('Integralmedica', '24681357000140', 'atendimento@integralmedica.com.br', true, now());
insert into marca (nome, cnpj, emailContato, ativa, dataCadastro) values ('Dux Nutrition', '13579246000101', 'contato@duxnutrition.com', true, now());
insert into marca (nome, cnpj, emailContato, ativa, dataCadastro) values ('Probiótica', '99887766000105', 'sac@probiotica.com.br', false, now());
insert into marca (nome, cnpj, emailContato, ativa, dataCadastro) values ('Essential Nutrition', '21000000000161', 'contato@essentialnutrition.com.br', true, now());
insert into marca (nome, cnpj, emailContato, ativa, dataCadastro) values ('Vitafor', '22000000000124', 'sac@vitafor.com.br', true, now());
insert into marca (nome, cnpj, emailContato, ativa, dataCadastro) values ('Atlhetica Nutrition', '23000000000197', 'atendimento@atlhetica.com.br', true, now());
insert into marca (nome, cnpj, emailContato, ativa, dataCadastro) values ('Black Skull', '24000000000150', 'contato@blackskull.com.br', true, now());
insert into marca (nome, cnpj, emailContato, ativa, dataCadastro) values ('Darkness', '25000000000112', 'sac@darkness.com.br', true, now());

-- ---------- Fornecedores (10) ----------
insert into fornecedor (razaoSocial, cnpj, telefone, email, dataCadastro) values ('Distribuidora Fit Norte LTDA', '55443322000105', '63999990001', 'compras@fitnorte.com.br', now());
insert into fornecedor (razaoSocial, cnpj, telefone, email, dataCadastro) values ('Nutri Atacado Brasil S.A.', '77123456000173', '1133330002', 'vendas@nutriatacado.com.br', now());
insert into fornecedor (razaoSocial, cnpj, telefone, email, dataCadastro) values ('Suplementos Centro-Oeste ME', '33112244000115', '62988880003', 'contato@supco.com.br', now());
insert into fornecedor (razaoSocial, cnpj, telefone, email, dataCadastro) values ('BioSul Distribuição Esportiva LTDA', '31000000000115', '5132221100', 'comercial@biosulfit.com.br', now());
insert into fornecedor (razaoSocial, cnpj, telefone, email, dataCadastro) values ('Paulista Logística Nutricional S.A.', '32000000000188', '1140028922', 'pedidos@paulistanutri.com.br', now());
insert into fornecedor (razaoSocial, cnpj, telefone, email, dataCadastro) values ('Cerrado Suplementos Atacado LTDA', '33000000000140', '61991112233', 'atacado@cerradosup.com.br', now());
insert into fornecedor (razaoSocial, cnpj, telefone, email, dataCadastro) values ('Minas Proteínas Comércio LTDA', '34000000000103', '3133445566', 'vendas@minasproteinas.com.br', now());
insert into fornecedor (razaoSocial, cnpj, telefone, email, dataCadastro) values ('Atlântico Nutrição Importadora S.A.', '35000000000176', '2135557788', 'importacao@atlanticonutri.com.br', now());
insert into fornecedor (razaoSocial, cnpj, telefone, email, dataCadastro) values ('Paraná Saúde & Performance LTDA', '36000000000139', '4130102030', 'contato@paranaperformance.com.br', now());
insert into fornecedor (razaoSocial, cnpj, telefone, email, dataCadastro) values ('Nordeste Fit Distribuidora LTDA', '37000000000100', '7132456789', 'vendas@nordestefit.com.br', now());

-- ---------- Sabores (10) ----------
insert into sabor (nome, descricao, dataCadastro) values ('Chocolate', 'Cacau intenso.', now());
insert into sabor (nome, descricao, dataCadastro) values ('Baunilha', 'Baunilha cremosa.', now());
insert into sabor (nome, descricao, dataCadastro) values ('Morango', 'Morango com toque de iogurte.', now());
insert into sabor (nome, descricao, dataCadastro) values ('Cookies and Cream', 'Biscoito com creme.', now());
insert into sabor (nome, descricao, dataCadastro) values ('Natural', 'Sem sabor adicionado.', now());
insert into sabor (nome, descricao, dataCadastro) values ('Doce de Leite', 'Doce de leite mineiro cremoso.', now());
insert into sabor (nome, descricao, dataCadastro) values ('Banana com Canela', 'Banana madura com toque de canela.', now());
insert into sabor (nome, descricao, dataCadastro) values ('Capuccino', 'Café espresso com chocolate e leite.', now());
insert into sabor (nome, descricao, dataCadastro) values ('Frutas Vermelhas', 'Mix refrescante de frutas vermelhas.', now());
insert into sabor (nome, descricao, dataCadastro) values ('Coco', 'Coco cremoso tropical.', now());

-- ---------- Produtos (12 — marca, categoria e fornecedor pelos ids acima) ----------
insert into produto (nome, descricao, preco, pesoGramas, estoque, tipoProteina, ativo, id_categoria, id_marca, id_fornecedor, dataCadastro) values ('Whey Protein Concentrado 1kg', 'Whey concentrado com 24g de proteína por dose.', 129.90, 1000, 40, 1, true, 1, 1, 1, now());
insert into produto (nome, descricao, preco, pesoGramas, estoque, tipoProteina, ativo, id_categoria, id_marca, id_fornecedor, dataCadastro) values ('Whey Isolado 900g', 'Isolado de alta pureza, baixo teor de lactose.', 219.90, 900, 25, 2, true, 1, 4, 2, now());
insert into produto (nome, descricao, preco, pesoGramas, estoque, tipoProteina, ativo, id_categoria, id_marca, id_fornecedor, dataCadastro) values ('100% Whey Max 2kg', 'Blend de concentrado e isolado.', 239.00, 2000, 18, 4, true, 1, 2, 2, now());
insert into produto (nome, descricao, preco, pesoGramas, estoque, tipoProteina, ativo, id_categoria, id_marca, id_fornecedor, dataCadastro) values ('Whey Hidrolisado 1kg', 'Absorção rápida para o pós-treino.', 289.90, 1000, 8, 3, true, 1, 3, 3, now());
insert into produto (nome, descricao, preco, pesoGramas, estoque, tipoProteina, ativo, id_categoria, id_marca, id_fornecedor, dataCadastro) values ('Proteína Vegana de Ervilha 600g', 'Blend de ervilha e arroz.', 149.90, 600, 15, 5, true, 2, 1, 1, now());
insert into produto (nome, descricao, preco, pesoGramas, estoque, tipoProteina, ativo, id_categoria, id_marca, id_fornecedor, dataCadastro) values ('Barra Protein Crisp 12un', 'Caixa com 12 barras de 45g.', 99.90, 540, 30, 4, true, 3, 3, 3, now());
insert into produto (nome, descricao, preco, pesoGramas, estoque, tipoProteina, ativo, id_categoria, id_marca, id_fornecedor, dataCadastro) values ('Whey Isolado Zero Lactose 450g', 'Versão compacta do isolado.', 129.00, 450, 0, 2, false, 1, 4, 2, now());
insert into produto (nome, descricao, preco, pesoGramas, estoque, tipoProteina, ativo, id_categoria, id_marca, id_fornecedor, dataCadastro) values ('Proteína de Soja 900g', 'Isolado proteico de soja.', 119.90, 900, 12, 5, true, 2, 2, 1, now());
insert into produto (nome, descricao, preco, pesoGramas, estoque, tipoProteina, ativo, id_categoria, id_marca, id_fornecedor, dataCadastro) values ('Isofort Whey Isolado 900g', 'Proteína isolada com alta concentração de BCAA.', 249.90, 900, 22, 2, true, 1, 7, 4, now());
insert into produto (nome, descricao, preco, pesoGramas, estoque, tipoProteina, ativo, id_categoria, id_marca, id_fornecedor, dataCadastro) values ('Cacao Whey Essential 450g', 'Whey hidrolisado e isolado com cacau puro.', 189.00, 450, 14, 3, true, 1, 6, 5, now());
insert into produto (nome, descricao, preco, pesoGramas, estoque, tipoProteina, ativo, id_categoria, id_marca, id_fornecedor, dataCadastro) values ('Caseína Micelar 900g', 'Liberação prolongada de aminoácidos durante o sono.', 209.90, 900, 10, 4, true, 7, 8, 6, now());
insert into produto (nome, descricao, preco, pesoGramas, estoque, tipoProteina, ativo, id_categoria, id_marca, id_fornecedor, dataCadastro) values ('Mass Monster 3kg', 'Hipercalórico com blend proteico e carboidratos complexos.', 159.90, 3000, 20, 4, true, 9, 10, 7, now());

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
insert into produto_sabor (id_produto, id_sabor) values (9, 6);
insert into produto_sabor (id_produto, id_sabor) values (9, 9);
insert into produto_sabor (id_produto, id_sabor) values (10, 1);
insert into produto_sabor (id_produto, id_sabor) values (10, 8);
insert into produto_sabor (id_produto, id_sabor) values (11, 2);
insert into produto_sabor (id_produto, id_sabor) values (11, 10);
insert into produto_sabor (id_produto, id_sabor) values (12, 1);
insert into produto_sabor (id_produto, id_sabor) values (12, 7);

-- ---------- Clientes (10 — Endereco -> Pessoa -> Telefone -> Cliente) ----------
insert into endereco (cep, logradouro, numero, complemento, bairro, id_municipio, dataCadastro) values ('77001002', 'Quadra 101 Sul Avenida LO 1', '10', 'Sala 2', 'Plano Diretor Sul', (select id from municipio where codigoIbge = '1721000'), now());
insert into endereco (cep, logradouro, numero, complemento, bairro, id_municipio, dataCadastro) values ('01310100', 'Avenida Paulista', '1578', null, 'Bela Vista', (select id from municipio where codigoIbge = '3550308'), now());
insert into endereco (cep, logradouro, numero, complemento, bairro, id_municipio, dataCadastro) values ('74115030', 'Rua 9', '250', 'Apto 1201', 'Setor Oeste', (select id from municipio where codigoIbge = '5208707'), now());
insert into endereco (cep, logradouro, numero, complemento, bairro, id_municipio, dataCadastro) values ('77804010', 'Avenida Cônego João Lima', '1450', 'Loja 1', 'Centro', (select id from municipio where codigoIbge = '1702109'), now());
insert into endereco (cep, logradouro, numero, complemento, bairro, id_municipio, dataCadastro) values ('77402010', 'Avenida Goiás', '880', null, 'Centro', (select id from municipio where codigoIbge = '1709500'), now());
insert into endereco (cep, logradouro, numero, complemento, bairro, id_municipio, dataCadastro) values ('70040010', 'SCS Quadra 4 Bloco A', '120', 'Sala 304', 'Asa Sul', (select id from municipio where codigoIbge = '5300108'), now());
insert into endereco (cep, logradouro, numero, complemento, bairro, id_municipio, dataCadastro) values ('22041001', 'Avenida Atlântica', '2100', 'Apto 502', 'Copacabana', (select id from municipio where codigoIbge = '3304557'), now());
insert into endereco (cep, logradouro, numero, complemento, bairro, id_municipio, dataCadastro) values ('30130170', 'Avenida Afonso Pena', '1900', null, 'Centro', (select id from municipio where codigoIbge = '3106200'), now());
insert into endereco (cep, logradouro, numero, complemento, bairro, id_municipio, dataCadastro) values ('80010000', 'Rua XV de Novembro', '650', 'Conj. 801', 'Centro', (select id from municipio where codigoIbge = '4106902'), now());
insert into endereco (cep, logradouro, numero, complemento, bairro, id_municipio, dataCadastro) values ('90010150', 'Rua dos Andradas', '1100', 'Sala 45', 'Centro Histórico', (select id from municipio where codigoIbge = '4314902'), now());

insert into pessoa (nome, email, cpf, dataNascimento, genero, id_endereco, dataCadastro) values ('Ana Martins', 'ana.martins@fitwork.com', '52998224725', '1994-03-12', 'Feminino', 1, now());
insert into pessoa (nome, email, cpf, dataNascimento, genero, id_endereco, dataCadastro) values ('Bruno Costa', 'bruno.costa@fitwork.com', '12345678909', '1988-07-25', 'Masculino', 2, now());
insert into pessoa (nome, email, cpf, dataNascimento, genero, id_endereco, dataCadastro) values ('Carla Nunes', 'carla.nunes@fitwork.com', '11144477735', '1999-11-08', 'Feminino', 3, now());
insert into pessoa (nome, email, cpf, dataNascimento, genero, id_endereco, dataCadastro) values ('Diego Almeida', 'diego.almeida@fitwork.com', '20123456703', '1991-02-18', 'Masculino', 4, now());
insert into pessoa (nome, email, cpf, dataNascimento, genero, id_endereco, dataCadastro) values ('Eduarda Ferreira', 'eduarda.ferreira@fitwork.com', '20246913401', '1996-06-30', 'Feminino', 5, now());
insert into pessoa (nome, email, cpf, dataNascimento, genero, id_endereco, dataCadastro) values ('Felipe Rocha', 'felipe.rocha@fitwork.com', '20370370155', '1985-09-14', 'Masculino', 6, now());
insert into pessoa (nome, email, cpf, dataNascimento, genero, id_endereco, dataCadastro) values ('Gabriela Lima', 'gabriela.lima@fitwork.com', '20493826858', '1993-12-03', 'Feminino', 7, now());
insert into pessoa (nome, email, cpf, dataNascimento, genero, id_endereco, dataCadastro) values ('Henrique Souza', 'henrique.souza@fitwork.com', '20617283591', '1990-04-22', 'Masculino', 8, now());
insert into pessoa (nome, email, cpf, dataNascimento, genero, id_endereco, dataCadastro) values ('Isabela Oliveira', 'isabela.oliveira@fitwork.com', '20740740261', '1997-08-19', 'Feminino', 9, now());
insert into pessoa (nome, email, cpf, dataNascimento, genero, id_endereco, dataCadastro) values ('João Pereira', 'joao.pereira@fitwork.com', '20864196946', '1987-01-27', 'Masculino', 10, now());

insert into telefone (codigoArea, numero, whatsapp, id_pessoa, dataCadastro) values ('63', '999991111', true, 1, now());
insert into telefone (codigoArea, numero, whatsapp, id_pessoa, dataCadastro) values ('11', '988882222', true, 2, now());
insert into telefone (codigoArea, numero, whatsapp, id_pessoa, dataCadastro) values ('11', '33334444', false, 2, now());
insert into telefone (codigoArea, numero, whatsapp, id_pessoa, dataCadastro) values ('62', '977773333', false, 3, now());
insert into telefone (codigoArea, numero, whatsapp, id_pessoa, dataCadastro) values ('63', '991114444', true, 4, now());
insert into telefone (codigoArea, numero, whatsapp, id_pessoa, dataCadastro) values ('63', '992225555', true, 5, now());
insert into telefone (codigoArea, numero, whatsapp, id_pessoa, dataCadastro) values ('61', '983336666', true, 6, now());
insert into telefone (codigoArea, numero, whatsapp, id_pessoa, dataCadastro) values ('21', '984447777', false, 7, now());
insert into telefone (codigoArea, numero, whatsapp, id_pessoa, dataCadastro) values ('31', '985558888', true, 8, now());
insert into telefone (codigoArea, numero, whatsapp, id_pessoa, dataCadastro) values ('41', '986669999', true, 9, now());
insert into telefone (codigoArea, numero, whatsapp, id_pessoa, dataCadastro) values ('51', '987770000', true, 10, now());

insert into cliente (id_pessoa, aceitaNovidades, ativo, dataCadastro) values (1, true, true, now());
insert into cliente (id_pessoa, aceitaNovidades, ativo, dataCadastro) values (2, false, true, now());
insert into cliente (id_pessoa, aceitaNovidades, ativo, dataCadastro) values (3, true, true, now());
insert into cliente (id_pessoa, aceitaNovidades, ativo, dataCadastro) values (4, true, true, now());
insert into cliente (id_pessoa, aceitaNovidades, ativo, dataCadastro) values (5, false, true, now());
insert into cliente (id_pessoa, aceitaNovidades, ativo, dataCadastro) values (6, true, true, now());
insert into cliente (id_pessoa, aceitaNovidades, ativo, dataCadastro) values (7, true, false, now());
insert into cliente (id_pessoa, aceitaNovidades, ativo, dataCadastro) values (8, false, true, now());
insert into cliente (id_pessoa, aceitaNovidades, ativo, dataCadastro) values (9, true, true, now());
insert into cliente (id_pessoa, aceitaNovidades, ativo, dataCadastro) values (10, true, true, now());
