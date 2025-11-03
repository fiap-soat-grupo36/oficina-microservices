package br.com.fiap.oficina.shared.utils;

import java.util.regex.Pattern;

public class ClienteValidator {

    public static final String REGEX = "[^\\d]";

    private ClienteValidator() {
    }

    private static final Pattern emailRegex = Pattern.compile("^[\\w\\.-]+@[\\w\\.-]+\\.\\w{2,}$");

    public static boolean emailValido(String email) {
        return email != null && emailRegex.matcher(email).matches();
    }

    public static boolean cpfValido(String cpf) {
        String cpfLimpo = limparDocumento(cpf);
        if (cpfLimpo.length() != 11 || cpfLimpo.matches("(\\d)\\1{10}")) return false;
        int dv1 = calcularDigitoVerificadorCpf(cpfLimpo.substring(0, 9));
        int dv2 = calcularDigitoVerificadorCpf(cpfLimpo.substring(0, 9) + dv1);

        return cpfLimpo.equals(cpfLimpo.substring(0, 9) + dv1 + dv2);
    }

    public static boolean cnpjValido(String cnpj) {
        String cnpjLimpo = limparDocumento(cnpj);
        if (cnpjLimpo.length() != 14 || cnpjLimpo.matches("(\\d)\\1{13}")) return false;

        int dv1 = calcularDigitoVerificadorCnpj(cnpjLimpo.substring(0, 12));
        int dv2 = calcularDigitoVerificadorCnpj(cnpjLimpo.substring(0, 12) + dv1);

        return cnpjLimpo.equals(cnpjLimpo.substring(0, 12) + dv1 + dv2);
    }

    public static boolean validarCliente(Object clienteRequest) {
        // Método genérico para aceitar qualquer DTO
        // Implementação específica no service
        return true;
    }

    public static String formatarCnpj(String cnpj) {
        if (cnpj == null) {
            return null;
        }
        String cnpjLimpo = cnpj.replaceAll(REGEX, "");

        if (cnpjLimpo.length() != 14) {
            return cnpj;
        }
        return cnpjLimpo.replaceFirst("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
    }

    public static String formatarCpf(String cpf) {
        if (cpf == null) {
            return null;
        }
        String cpfLimpo = cpf.replaceAll(REGEX, "");

        if (cpfLimpo.length() != 11) {
            return cpf;
        }
        return cpfLimpo.replaceFirst("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
    }

    private static String limparDocumento(String documento) {
        return documento == null ? "" : documento.replaceAll(REGEX, "");
    }

    private static int calcularDigitoVerificadorCpf(String base) {
        int soma = 0;
        for (int i = 0; i < base.length(); i++) {
            soma += Character.getNumericValue(base.charAt(i)) * (base.length() + 1 - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static int calcularDigitoVerificadorCnpj(String base) {
        int[] pesos = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int soma = 0;
        for (int i = 0; i < base.length(); i++) {
            soma += Character.getNumericValue(base.charAt(i)) * pesos[pesos.length - base.length() + i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}