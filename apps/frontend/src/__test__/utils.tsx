/* eslint-disable react-refresh/only-export-components */
import { type ReactElement, type ReactNode, type JSX } from 'react'
import { render, type RenderOptions } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'

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
                <>{children}</>
            </BrowserRouter>
        </QueryClientProvider>
    )
}

const customRender = (
    ui: ReactElement,
    options?: Omit<RenderOptions, 'wrapper'>,
) => render(ui, { wrapper: AllTheProviders, ...options })

export * from '@testing-library/react'
export { customRender as render }