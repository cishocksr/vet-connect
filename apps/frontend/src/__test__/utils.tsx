/* eslint-disable react-refresh/only-export-components */
import { type ReactElement, type ReactNode, type JSX } from 'react'
import { render, type RenderOptions, renderHook, type RenderHookOptions } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ThemeProvider } from '@/contexts/theme-context'

interface AllTheProvidersProps {
    children: ReactNode
}

const createTestQueryClient = () => new QueryClient({
    defaultOptions: {
        queries: {
            retry: false,
        },
    },
})

let testQueryClient: QueryClient

const AllTheProviders = ({ children }: AllTheProvidersProps): JSX.Element => {
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
) => {
    testQueryClient = createTestQueryClient()
    return render(ui, { wrapper: AllTheProviders, ...options })
}

const customRenderHook = <Result, Props>(
    hook: (initialProps: Props) => Result,
    options?: Omit<RenderHookOptions<Props>, 'wrapper'>,
) => {
    testQueryClient = createTestQueryClient()
    return renderHook(hook, { wrapper: AllTheProviders, ...options })
}

export * from '@testing-library/react'
export { customRender as render, customRenderHook as renderHook }
