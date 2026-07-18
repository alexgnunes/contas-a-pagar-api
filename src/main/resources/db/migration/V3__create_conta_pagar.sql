CREATE TABLE conta_pagar (
    id UUID PRIMARY KEY,
    descricao VARCHAR(255) NOT NULL,
    valor NUMERIC(19, 2) NOT NULL,
    data_vencimento DATE NOT NULL,
    data_pagamento DATE,
    situacao VARCHAR(20) NOT NULL,
    fornecedor_id UUID NOT NULL,
    CONSTRAINT fk_conta_pagar_fornecedor
        FOREIGN KEY (fornecedor_id) REFERENCES fornecedor (id)
        ON DELETE RESTRICT
);

CREATE INDEX idx_conta_pagar_fornecedor_id ON conta_pagar (fornecedor_id);
