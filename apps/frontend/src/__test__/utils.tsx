/* eslint-disable react-refresh/only-export-components */
import { type ReactElement, type ReactNode, type JSX } from 'react'
import { render, type RenderOptions, renderHook, type RenderHookOptions } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ThemeProvider } from '@/contexts/theme-context'

interface AllTheProvidersProps {
    children: ReactNode
}

const AllTheProviders = ({ children }: AllTheProvidersProps): JSX.Element => {
    const testQueryClient = new QueryClient({
        defaultOptions: {
            queries: {
                retry: false,
            },
        },
    })

    return (
        <QueryClientProvider client={testQueryClient}>
            <BrowserRouter>
                <ThemeProvider>
                    <>{children}</>
                </ThemeProvider>
            </BrowserRouter>
        </QueryClientProvider>
    )
}

const customRender = (
    ui: ReactElement,
    options?: Omit<RenderOptions, 'wrapper'>,
) => render(ui, { wrapper: AllTheProviders, ...options })

const customRenderHook = <Result, Props>(
    hook: (initialProps: Props) => Result,
    options?: Omit<RenderHookOptions<Props>, 'wrapper'>,
) => renderHook(hook, { wrapper: AllTheProviders, ...options })

export * from '@testing-library/react'
export { customRender as render, customRenderHook as renderHook }