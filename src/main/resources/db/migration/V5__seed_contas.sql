INSERT INTO conta_pagar (id, descricao, valor, data_vencimento, data_pagamento, situacao, fornecedor_id) VALUES
    ('aaaaaaaa-0001-0001-0001-000000000001', 'Energia Eletrica - Seed', 850.00,
        (CURRENT_DATE + INTERVAL '10 days')::date, NULL, 'PENDENTE', '11111111-1111-1111-1111-111111111111'),
    ('aaaaaaaa-0001-0001-0001-000000000002', 'Internet e Telefonia - Seed', 320.50,
        (CURRENT_DATE - INTERVAL '5 days')::date, (CURRENT_DATE - INTERVAL '3 days')::date, 'PAGO', '22222222-2222-2222-2222-222222222222'),
    ('aaaaaaaa-0001-0001-0001-000000000003', 'Material de Escritorio - Seed', 1500.00,
        (CURRENT_DATE + INTERVAL '20 days')::date, NULL, 'PENDENTE', '33333333-3333-3333-3333-333333333333'),
    ('aaaaaaaa-0001-0001-0001-000000000004', 'Agua e Esgoto - Seed', 45.90,
        (CURRENT_DATE - INTERVAL '15 days')::date, (CURRENT_DATE - INTERVAL '10 days')::date, 'PAGO', '11111111-1111-1111-1111-111111111111'),
    ('aaaaaaaa-0001-0001-0001-000000000005', 'Aluguel do Deposito - Seed', 999.99,
        (CURRENT_DATE + INTERVAL '2 days')::date, NULL, 'PENDENTE', '22222222-2222-2222-2222-222222222222');
