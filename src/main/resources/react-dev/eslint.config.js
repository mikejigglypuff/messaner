import react from 'eslint-plugin-react';
import reactHooks from 'eslint-plugin-react-hooks';
import babelParser from '@babel/eslint-parser';

export default [
    {
        files: ['src/**/*.{js, jsx}'],
        ignores: ['.config/*'],
        languageOptions: {
            ecmaVersion: 6,
            sourceType: 'module',
            parser: babelParser,
            parserOptions: {
                babelOptions: {
                   'presets': ['@babel/preset-react']
                },
                requireConfigFile: false,
                ecmaFeatures: {
                    jsx: true
                }
            },
        },
        plugins: {
            react,
            'react-hooks': reactHooks,
        },
        rules: {
            'no-undef': 'warn',
            'no-unused-vars': 'warn',
            'react/react-in-jsx-scope': 'off',
            'react/jsx-uses-react': 'off',
            'react-hooks/rules-of-hooks': 'error',
            'react-hooks/exhaustive-deps': 'warn'
        }
    }
];