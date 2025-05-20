# Contributing to Originate

Thank you for your interest in contributing to Originate! This document outlines the process for contributing to this open-source supply chain traceability solution.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Process](#development-process)
- [Pull Request Process](#pull-request-process)
- [Issue Guidelines](#issue-guidelines)
- [Coding Standards](#coding-standards)
- [Community](#community)

## Code of Conduct

This project and everyone participating in it is governed by our Code of Conduct. By participating, you are expected to uphold this code. Please report unacceptable behavior to originate-conduct@cardanofoundation.org.

## Getting Started

### Prerequisites

Before contributing, please ensure you have:

- Read and understood our README.md
- Set up your development environment according to our setup instructions
- Reviewed existing issues and pull requests to avoid duplicating efforts

### First Contribution

New to contributing? Here are some good ways to get started:

1. **Documentation improvements** - Fix typos, improve clarity, or add missing documentation
2. **Good first issue** - Look for issues labeled `good-first-issue`
3. **Bug reports** - Help us identify and reproduce bugs
4. **Feature requests** - Propose new functionality that aligns with project goals

## Development Process

### Branch Strategy

- **main** - Production-ready code
- **develop** - Integration branch for features
- **Feature branches** - `feature/your-feature-name`
- **Bug fixes** - `fix/issue-description`

### Workflow

1. **Fork the repository** and create your branch from `develop`
2. **Make your changes** following our coding standards
3. **Add tests** for any new functionality
4. **Update documentation** as needed
5. **Ensure all tests pass** before submitting
6. **Submit a pull request** with a clear description

## Pull Request Process

### Before Submitting

- [ ] Your branch is up to date with the target branch
- [ ] All tests pass locally
- [ ] Code follows project style guidelines
- [ ] Documentation is updated if needed
- [ ] Commit messages are clear and descriptive

### PR Requirements

1. **Clear title and description** - Explain what changes you made and why
2. **Reference related issues** - Use `Fixes #123` or `Closes #123`
3. **Small, focused changes** - Keep PRs manageable and focused on a single concern
4. **Screenshots or demos** - Include visuals for UI changes
5. **Breaking changes** - Clearly document any breaking changes

### Review Process

- **Maintainer review required** - All PRs require approval from at least one maintainer
- **CI/CD checks** - All automated checks must pass
- **Community feedback** - Community members are encouraged to review and comment
- **Response time** - Maintainers will respond on a best effort basis

### Merging

- PRs are typically **squash merged** to maintain a clean history
- **Delete feature branches** after merging
- **Release notes** will be updated by maintainers

## Issue Guidelines

### Bug Reports

When reporting bugs, please include:

- **Clear title** and description
- **Steps to reproduce** the issue
- **Expected vs actual behavior**
- **Environment details** (OS, browser, version, etc.)
- **Screenshots or logs** if applicable

### Feature Requests

For new features, please provide:

- **Problem statement** - What problem does this solve?
- **Proposed solution** - How should it work?
- **Use cases** - Who would benefit and how?
- **Alternatives considered** - What other solutions did you consider?

### Security Issues

**Do not** report security vulnerabilities through public GitHub issues. Follow instead the [SECURITY](./SECURITY.md) instructions.

## Coding Standards

### General Guidelines

- **Write clear, readable code** - Code should be self-documenting
- **Follow existing patterns** - Maintain consistency with the existing codebase
- **Comment complex logic** - Explain the "why," not just the "what"
- **Write tests** - Aim for good test coverage
- **Keep it simple** - Prefer simple solutions over complex ones

### Style Guidelines

- Use meaningful variable and function names
- Keep functions small and focused
- Follow established naming conventions
- Maintain consistent indentation and formatting

### Testing

- **Unit tests** for individual functions and components
- **Integration tests** for feature workflows
- **End-to-end tests** for critical user journeys
- **Test coverage** should not decrease with new contributions

## Community

### Communication Channels

- **GitHub Issues** - For bug reports and feature requests
- **GitHub Discussions** - For general questions and community discussions
- **Pull Requests** - For code review and technical discussions

### Getting Help

- Check existing documentation and issues first
- Use GitHub Discussions for questions
- Tag appropriate maintainers for urgent issues
- Be patient and respectful when seeking help

### Recognition

Contributors are recognized in:

- **CONTRIBUTORS.md** file
- **Release notes** for significant contributions
- **GitHub contributors** page

## Licensing

By contributing to Originate, you agree that your contributions will be licensed under the Apache License 2.0. All original contributions must be your own work or properly attributed if derived from other sources.

## Questions?

If you have questions about contributing, please:

1. Check this document and other project documentation
2. Search existing GitHub issues and discussions
3. Create a new discussion in GitHub Discussions
4. Contact maintainers directly for urgent matters

Thank you for contributing to Originate and helping build better supply chain transparency!