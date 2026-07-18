package br.com.alexnunes.contaspagar.domain.conta;

import br.com.alexnunes.contaspagar.domain.conta.enums.Situacao;
import br.com.alexnunes.contaspagar.domain.conta.exception.SituacaoInvalidaException;
import br.com.alexnunes.contaspagar.domain.conta.exception.ValorInvalidoException;
import br.com.alexnunes.contaspagar.domain.fornecedor.Fornecedor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "conta_pagar")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Situacao situacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fornecedor_id", nullable = false)
    private Fornecedor fornecedor;

    public Conta(String descricao, BigDecimal valor, LocalDate dataVencimento, Fornecedor fornecedor) {
        validarValor(valor);
        this.descricao = descricao;
        this.valor = valor;
        this.dataVencimento = dataVencimento;
        this.fornecedor = fornecedor;
        this.situacao = Situacao.PENDENTE;
    }

    private static void validarValor(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new ValorInvalidoException("Valor deve ser positivo");
        }
    }

    public void pagar() {
        if (situacao == Situacao.CANCELADO) {
            throw new SituacaoInvalidaException("Conta cancelada não pode ser paga");
        }
        if (situacao == Situacao.PAGO) {
            throw new SituacaoInvalidaException("Conta já está paga");
        }
        this.situacao = Situacao.PAGO;
        this.dataPagamento = LocalDate.now();
    }

    public void cancelar() {
        if (situacao == Situacao.PAGO) {
            throw new SituacaoInvalidaException("Conta paga não pode ser cancelada");
        }
        if (situacao == Situacao.CANCELADO) {
            throw new SituacaoInvalidaException("Conta já está cancelada");
        }
        this.situacao = Situacao.CANCELADO;
    }

    public void alterarValor(BigDecimal novoValor) {
        garantirPendente("Valor só pode ser alterado enquanto a conta estiver pendente");
        validarValor(novoValor);
        this.valor = novoValor;
    }

    public void trocarFornecedor(Fornecedor novoFornecedor) {
        garantirPendente("Fornecedor só pode ser alterado enquanto a conta estiver pendente");
        this.fornecedor = novoFornecedor;
    }

    private void garantirPendente(String mensagem) {
        if (situacao != Situacao.PENDENTE) {
            throw new SituacaoInvalidaException(mensagem);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Conta other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
